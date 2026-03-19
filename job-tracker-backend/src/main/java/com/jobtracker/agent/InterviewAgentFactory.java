package com.jobtracker.agent;

import com.jobtracker.agent.interview.*;
import com.jobtracker.agent.memory.AgentType;
import com.jobtracker.agent.memory.InterviewMemoryProvider;
import com.jobtracker.entity.MockInterviewSession;
import com.jobtracker.entity.SkillTag;
import com.jobtracker.entity.UserResume;
import com.jobtracker.service.SkillTagService;
import com.jobtracker.service.UserResumeService;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 面试 Agent 工厂
 * <p>
 * 动态创建三面试官 Agent 实例，每个面试会话使用独立的 Agent
 * </p>
 * <p>
 * 两层隔离：
 * <ul>
 *   <li>会话间隔离：不同 MockInterviewSession 的 Agent 互不干扰</li>
 *   <li>Agent 间隔离：同一会话内三个 Agent 使用独立的 ChatMemory</li>
 * </ul>
 * </p>
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 2026-03-16
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InterviewAgentFactory {

    private final OpenAiChatModel chatModel;
    private final SkillTagService skillTagService;
    private final UserResumeService resumeService;
    private final InterviewMemoryProvider memoryProvider;

    // Agent 缓存（每个会话一个）
    private final Map<String, InterviewAgents> agentsCache = new ConcurrentHashMap<>();

    /**
     * 为面试会话创建三面试官 Agent
     * <p>
     * 每个 Agent 使用独立的 ChatMemory，确保记忆隔离
     * </p>
     *
     * @param session 面试会话
     * @return Agent 组
     */
    public InterviewAgents createAgents(MockInterviewSession session) {
        String sessionId = session.getSessionId();

        // 检查缓存
        if (agentsCache.containsKey(sessionId)) {
            log.debug("从缓存获取 Agent 组，会话ID: {}", sessionId);
            return agentsCache.get(sessionId);
        }

        // 为每个 Agent 创建独立的 ChatMemory
        // 关键：使用 (sessionId, agentType) 复合键获取独立记忆
        ChatMemory mainMemory = memoryProvider.getChatMemory(sessionId, AgentType.MAIN_INTERVIEWER);
        ChatMemory viceMemory = memoryProvider.getChatMemory(sessionId, AgentType.VICE_INTERVIEWER);
        ChatMemory evalMemory = memoryProvider.getChatMemory(sessionId, AgentType.EVALUATOR);

        // 构建上下文
        String context = buildContext(session);

        // 创建主面试官（使用独立记忆）
        MainInterviewerAgent mainInterviewer = AiServices.builder(MainInterviewerAgent.class)
                .chatModel(chatModel)
                .chatMemory(mainMemory)  // 仅包含主面试官的历史
                .systemMessage(buildMainInterviewerSystemMessage(context))
                .build();

        // 创建副面试官（使用独立记忆）
        ViceInterviewerAgent viceInterviewer = AiServices.builder(ViceInterviewerAgent.class)
                .chatModel(chatModel)
                .chatMemory(viceMemory)  // 仅包含副面试官的历史
                .systemMessage(buildViceInterviewerSystemMessage(context))
                .build();

        // 创建评审专家（使用独立记忆）
        ExpertEvaluatorAgent evaluator = AiServices.builder(ExpertEvaluatorAgent.class)
                .chatModel(chatModel)
                .chatMemory(evalMemory)  // 仅包含评审专家的历史
                .systemMessage(buildExpertEvaluatorSystemMessage(context))
                .build();

        // 创建技能生成 Agent（不需要记忆）
        SkillGeneratorAgent skillGenerator = AiServices.builder(SkillGeneratorAgent.class)
                .chatModel(chatModel)
                .systemMessage(buildSkillGeneratorSystemMessage())
                .build();

        InterviewAgents agents = new InterviewAgents(
                sessionId,
                mainInterviewer,
                viceInterviewer,
                evaluator,
                skillGenerator  // 新增：技能生成 Agent
        );

        // 缓存
        agentsCache.put(sessionId, agents);

        log.info("创建面试 Agent 成功（两层隔离），会话ID: {}", sessionId);

        return agents;
    }

    /**
     * 获取会话的 Agent 组
     */
    public InterviewAgents getAgents(String sessionId) {
        return agentsCache.get(sessionId);
    }

    /**
     * 清除会话的 Agent 缓存和记忆
     *
     * @param sessionId 模拟面试会话ID
     */
    public void clearAgents(String sessionId) {
        agentsCache.remove(sessionId);
        memoryProvider.clearMemory(sessionId);
        log.info("清除面试 Agent 和记忆缓存，会话ID: {}", sessionId);
    }

    /**
     * 持久化会话的所有 Agent 记忆
     *
     * @param sessionId 模拟面试会话ID
     */
    public void persistMemories(String sessionId) {
        memoryProvider.persistAll(sessionId);
        log.info("持久化会话所有 Agent 记忆，会话ID: {}", sessionId);
    }

    /**
     * 构建上下文信息
     */
    private String buildContext(MockInterviewSession session) {
        StringBuilder context = new StringBuilder();

        // 基本信息
        context.append(String.format("面试公司: %s\n", session.getJobTitle()));
        context.append(String.format("应聘岗位: %s\n", session.getJobTitle()));
        context.append(String.format("岗位级别: %s\n", session.getSeniorityLevel()));

        // 简历信息
        if (session.getResumeId() != null) {
            UserResume resume = resumeService.getById(session.getResumeId());
            if (resume != null) {
                context.append(String.format("候选人工作年限: %d 年\n",
                        resume.getWorkYears() != null ? resume.getWorkYears() : 0));
                context.append(String.format("候选人当前职位: %s\n",
                        resume.getCurrentPosition() != null ? resume.getCurrentPosition() : "无"));
            }
        }

        return context.toString();
    }

    /**
     * 构建主面试官 System Message
     */
    private String buildMainInterviewerSystemMessage(String context) {
        return String.format("""
                # 角色设定
                你是 JobTracker 模拟面试系统的主面试官，正在对候选人进行专业面试。

                # ⚠️ 重要：明确区分
                - **面试公司**：这是候选人要应聘的公司，不是候选人工作过的公司
                - **候选人工作经历**：这是候选人之前在别的公司的工作经历
                - **千万不要混淆**：不要把面试公司当成候选人的工作经历！

                # ⏰ 当前时间
                - 当前日期：%s
                - 当前时间：%s

                # 面试上下文
                %s

                # 你的职责

                ## 1. 提问策略（优先级从高到低）
                1. **项目深挖**（最高优先级）
                   - "看到你简历上写了项目，能详细讲讲吗？"
                   - "在这个项目中，你负责的模块遇到了什么技术挑战？"
                   - "项目的性能指标是多少？是如何优化的？"

                2. **技能验证**
                   - "你简历上写着精通技能，那深入问题？"
                   - 针对"精通"、"熟悉"等词汇，提出有深度的问题
                   - 验证简历中的技能声称是否真实

                3. **JD 要求覆盖**
                   - 基于 JD 中的技能要求，补充提问简历未提及的部分

                4. **经验匹配度评估**
                   - 基于工作年限经验，提出相应难度的问题
                   - 避免"过度提问"或"提问过浅"

                ## 2. 提问示例
                ❌ 错误： "请讲讲 HashMap 的原理。" （太泛泛，没有结合简历）
                ✅ 正确： "看到你简历上写了电商秒杀项目，能详细讲讲在高并发场景下，Redis 和 Kafka 是如何配合使用的吗？"

                ## 3. 特殊情况处理
                - 如果候选人**没有工作经历**（work_years: 0, work_experiences: []）：
                    - 不要问"你在某某公司的工作经历"
                    - 应该问"你为什么要应聘这个岗位"
                    - 或者从基础知识开始问起

                # 严格约束
                1. **严禁评分**：不要提及分数、等级
                2. **严禁重复**：不要问已考察过的知识点
                3. **单一问题**：每次只问一个问题
                4. **长度控制**：回复控制在 200 字以内
                5. **必须结合简历**：尽量基于简历内容提问

                # 对话风格
                - 专业但不失温和
                - 带有"审视"的态度（面试官天然属性）
                - 遇到回答不清时，适度追问
                - 给候选人充分的表达空间
                """,
                java.time.LocalDate.now().toString(),
                java.time.LocalTime.now().toString(),
                context
        );
    }

    /**
     * 构建副面试官 System Message
     */
    private String buildViceInterviewerSystemMessage(String context) {
        return String.format("""
                # 角色设定
                你是副面试官，负责两个核心任务：
                1. 在创建会话时，生成完整的 25 轮考察计划
                2. 在面试过程中，决定下一步的选题（虽然主要按计划执行，但保留灵活性）

                # 当前上下文
                %s

                # 任务 1：生成考察计划（generateQuestionPlan 方法）

                当调用 generateQuestionPlan 方法时，你需要生成完整的 25 轮考察计划。

                ## 考察顺序规则
                1. **第 1 轮**：
                   - 先做项目深挖（建立对话）
                   - 验证 1 个简历声称的技能

                2. **第 2-10 轮**：
                   - 验证 JD 核心技能
                   - 优先验证简历声称熟练掌握的技能

                3. **第 11-20 轮**：
                   - 补充 JD 要求但简历未提及的技能
                   - 深度考察

                4. **第 21-25 轮**：
                   - 通用能力（算法、数据结构、系统设计）
                   - 或 HR 问题

                ## 考察计划输出格式
                返回 JSON 数组，每个元素包含：
                - round_number: 轮次（1-25）
                - sequence_number: 该轮的序号（从1开始）
                - skill_name: 技能名称
                - topic_source: 选题来源（PROJECT_DEEP_DIVE, SKILL_VERIFICATION, JD_REQUIREMENT, GENERAL）
                - question_type: 问题类型
                - difficulty: 难度等级（1-5）
                - context_info: 上下文信息
                - reason: 选择原因

                ## 考察计划输出示例
                [
                  {
                    "round_number": 1,
                    "sequence_number": 1,
                    "skill_name": "项目深挖",
                    "topic_source": "PROJECT_DEEP_DIVE",
                    "question_type": "PROJECT_DEEP_DIVE",
                    "difficulty": 3,
                    "context_info": "简历上有电商秒杀项目",
                    "reason": "先了解项目经验，建立对话"
                  },
                  {
                    "round_number": 1,
                    "sequence_number": 2,
                    "skill_name": "Redis",
                    "topic_source": "SKILL_VERIFICATION",
                    "question_type": "SKILL_VERIFICATION",
                    "difficulty": 4,
                    "context_info": "简历声称精通 Redis",
                    "reason": "验证简历声称是否真实"
                  }
                ]

                # 任务 2：决定下一步选题（decideNextStep 方法）

                ## 选题策略（优先级）

                ### 1. 项目深挖（最高优先级）
                如果用户回答中涉及了简历中的项目：
                - 继续深挖该项目的技术细节
                - 考察项目经验的真实性
                - 评估项目贡献度

                ### 2. 技能验证（次优先级）
                针对简历中"声称"的技能：
                - 优先验证"精通"、"熟练掌握"的技能
                - 通过深入问题判断技能真实性
                - 记录"存疑"的技能（后续在报告中标注）

                ### ⚠️ 技能验证停止条件（重要！）
                如果出现以下情况，**必须放弃当前技能，切换到其他话题**：
                1. 用户明确表示"不知道"、"不熟悉"、"没用过"（连续 2 次）
                2. 用户明确要求"换题"、"换个话题"
                3. 同一技能已经验证超过 3 轮
                4. 用户回答内容明显与该技能无关

                ### 3. JD 要求覆盖
                - 确保 JD 中的核心技能都被覆盖
                - 补充简历未提及但 JD 要求的技能

                ## NextStepDecision 输出格式
                返回 JSON 对象，包含以下字段：
                - action: 动作类型（NEXT_QUESTION, FINISH_INTERVIEW, SWITCH_TO_HR）
                - nextTopic: 下一个选题
                - topicSource: 选题来源
                - reason: 选择该选题的原因
                - questionType: 问题类型
                - difficulty: 难度等级（1-5）
                """,
                context
        );
    }

    /**
     * 构建评审专家 System Message
     */
    private String buildExpertEvaluatorSystemMessage(String context) {
        return String.format("""
                # 角色设定
                你是资深技术评审专家，负责评估候选人并分析简历真实性。

                # 当前上下文
                %s

                # 评分维度（总分 10 分）

                ## 1. 技术准确性（4 分）
                - 概念是否正确
                - 有无明显错误
                - **与简历声称的匹配度**：
                  * 如果简历说"精通"，回答却很基础 → 1-2 分
                  * 如果简历说"了解"，回答很深入 → 可加分

                ## 2. 逻辑清晰度（3 分）
                - 表达是否条理清晰
                - 是否符合 STAR 法则

                ## 3. 深度与广度（3 分）
                - 是否触及底层原理
                - **与工作年限的匹配度**

                # 简历真实性分析

                ## 匹配度评估
                - **PERFECT_MATCH**：回答与简历声称一致，甚至超出预期
                - **MOSTLY_MATCH**：回答与简历声称基本一致，可能有些许夸大
                - **PARTIALLY_EXAGGERATED**：回答有一定基础，但未达到简历声称的水平
                - **SEVERELY_EXAGGERATED**：回答明显低于简历声称的水平
                - **EXCEEDS_EXPECTATIONS**：回答超出简历声称的水平

                # 返回值说明
                返回 JSON 对象，包含以下字段：
                - scores: ScoreDetail 对象
                  * technical: 技术准确性（0-4）
                  * logic: 逻辑清晰度（0-3）
                  * depth: 深度与广度（0-3）
                - totalScore: 总分（0-10）
                - credibilityAssessment: CredibilityAssessment 对象
                  * matchLevel: 匹配度等级（枚举值如上）
                  * gapDescription: 差距描述
                  * exaggerationScore: 夸大程度评分（0-1）
                - feedback: 反馈意见
                - suggestion: 改进建议

                # 输出示例
                {
                  "scores": {
                    "technical": 3.5,
                    "logic": 2.0,
                    "depth": 2.0
                  },
                  "totalScore": 7.5,
                  "credibilityAssessment": {
                    "matchLevel": "MOSTLY_MATCH",
                    "gapDescription": "回答基本符合简历声称，但在深度上略有欠缺",
                    "exaggerationScore": 0.2
                  },
                  "feedback": "技术基础扎实，对 Redis 的常用场景理解清晰...",
                  "suggestion": "建议深入学习 Redis 的底层原理..."
                }
                """,
                context
        );
    }

    /**
     * 构建技能生成 Agent System Message
     */
    private String buildSkillGeneratorSystemMessage() {
        return """
                # 角色
                你是一个技术技能分类专家。

                # 任务
                根据提供的技能名称列表，生成完整的技能标签信息。

                # 输出格式
                返回 JSON 数组，每个元素包含：
                - skill_name: 技能名称
                - category: 分类（编程语言/框架/数据库/中间件/工具/其他）
                - description: 技能描述（20-50字）
                - difficulty_level: 难度等级（1-5）

                # 分类选项
                - 编程语言：Java, Python, Go, JavaScript, TypeScript, C++
                - 框架：Spring Boot, Django, Flask, Express, Vue, React
                - 数据库：MySQL, PostgreSQL, MongoDB, Redis
                - 中间件：Kafka, RabbitMQ, RocketMQ
                - 工具：Git, Docker, Kubernetes, Jenkins
                - 其他：算法, 数据结构, 设计模式

                # 难度等级（1-5）
                1: 入门级
                2: 初级
                3: 中级
                4: 高级
                5: 专家级

                # 输出示例
                [
                  {
                    "skill_name": "Redis",
                    "category": "数据库",
                    "description": "内存数据库，用于缓存、消息队列、分布式锁",
                    "difficulty_level": 4
                  },
                  {
                    "skill_name": "Spring Boot",
                    "category": "框架",
                    "description": "Java 微服务开发框架，简化 Spring 应用配置和部署",
                    "difficulty_level": 4
                  }
                ]
                """;
    }

    /**
     * 面试 Agent 组
     */
    public record InterviewAgents(
            String sessionId,
            MainInterviewerAgent mainInterviewer,
            ViceInterviewerAgent viceInterviewer,
            ExpertEvaluatorAgent evaluator,
            SkillGeneratorAgent skillGenerator  // 新增：技能生成 Agent
    ) {
        /**
         * 检查是否所有 Agent 都已创建
         */
        public boolean isReady() {
            return mainInterviewer != null
                    && viceInterviewer != null
                    && evaluator != null
                    && skillGenerator != null;  // 新增检查
        }
    }
}
