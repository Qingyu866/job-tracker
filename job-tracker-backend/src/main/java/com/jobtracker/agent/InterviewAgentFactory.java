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

        InterviewAgents agents = new InterviewAgents(
                sessionId,
                mainInterviewer,
                viceInterviewer,
                evaluator
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
        context.append(String.format("公司: %s\n", session.getJobTitle()));
        context.append(String.format("岗位: %s\n", session.getJobTitle()));
        context.append(String.format("级别: %s\n", session.getSeniorityLevel()));

        // 简历信息
        if (session.getResumeId() != null) {
            UserResume resume = resumeService.getById(session.getResumeId());
            if (resume != null) {
                context.append(String.format("工作年限: %d 年\n",
                        resume.getWorkYears() != null ? resume.getWorkYears() : 0));
                context.append(String.format("当前职位: %s\n", resume.getCurrentPosition()));
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
                你是面试流程控制器，负责维护面试进度和智能选题。

                # 当前上下文
                %s

                # 选题策略（优先级）

                ## 1. 项目深挖（最高优先级）
                如果用户回答中涉及了简历中的项目：
                - 继续深挖该项目的技术细节
                - 考察项目经验的真实性
                - 评估项目贡献度

                ## 2. 技能验证（次优先级）
                针对简历中"声称"的技能：
                - 优先验证"精通"、"熟练掌握"的技能
                - 通过深入问题判断技能真实性
                - 记录"存疑"的技能（后续在报告中标注）

                ## 3. JD 要求覆盖
                - 确保 JD 中的核心技能都被覆盖
                - 补充简历未提及但 JD 要求的技能

                # 输出格式（严格 JSON）
                {
                  "action": "NEXT_QUESTION",
                  "next_topic": "Redis 集群 Slot 迁移",
                  "topic_source": "RESUME_SKILL",
                  "reason": "简历上写着精通 Redis，需要验证深度",
                  "question_type": "SKILL_VERIFICATION",
                  "difficulty": 4
                }

                # topic_source 说明
                - "PROJECT_DEEP_DIVE": 项目深挖
                - "SKILL_VERIFICATION": 技能验证
                - "JD_REQUIREMENT": JD 要求覆盖
                - "GENERAL": 通用问题
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
                - **完全匹配**：回答与简历声称一致
                - **部分夸大**：回答有一定基础，但未达到简历声称的水平
                - **严重夸大**：回答明显低于简历声称的水平
                - **超出预期**：回答超出简历声称的水平

                ## 输出格式（严格 JSON）
                {
                  "scores": {
                    "technical": 2.5,
                    "logic": 2.0,
                    "depth": 1.5
                  },
                  "total_score": 6.0,
                  "credibility_assessment": {
                    "match_level": "部分夸大",
                    "gap_description": "简历声称精通 Redis，但对集群模式的回答很基础",
                    "exaggeration_score": 0.6
                  },
                  "feedback": "用户知道 Redis 的基本概念，但对集群模式的 Slot 迁移机制不熟悉...",
                  "suggestion": "建议深入学习 Redis Cluster 的原理..."
                }
                """,
                context
        );
    }

    /**
     * 面试 Agent 组
     */
    public record InterviewAgents(
            String sessionId,
            MainInterviewerAgent mainInterviewer,
            ViceInterviewerAgent viceInterviewer,
            ExpertEvaluatorAgent evaluator
    ) {
        /**
         * 检查是否所有 Agent 都已创建
         */
        public boolean isReady() {
            return mainInterviewer != null
                    && viceInterviewer != null
                    && evaluator != null;
        }
    }
}
