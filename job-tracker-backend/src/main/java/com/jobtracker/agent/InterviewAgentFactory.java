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

        // 构建基本上下文
        String basicContext = buildContext(session);

        // ⭐ 获取快照（用于调用时的 @V 参数）
        String resumeSnapshot = session.getResumeSnapshot();
        String jdSnapshot = session.getJdSnapshot();

        // 创建主面试官（使用独立记忆）
        // ⚠️ System Message 通过 @SystemMessage(fromResource = ...) 自动加载
        MainInterviewerAgent mainInterviewer = AiServices.builder(MainInterviewerAgent.class)
                .chatModel(chatModel)
                .chatMemory(mainMemory)
                .build();

        // 创建副面试官（使用独立记忆）
        ViceInterviewerAgent viceInterviewer = AiServices.builder(ViceInterviewerAgent.class)
                .chatModel(chatModel)
                .chatMemory(viceMemory)
                .build();

        // 创建评审专家（使用独立记忆）
        ExpertEvaluatorAgent evaluator = AiServices.builder(ExpertEvaluatorAgent.class)
                .chatModel(chatModel)
                .chatMemory(evalMemory)
                .build();

        // 创建技能生成 Agent（不需要记忆）
        SkillGeneratorAgent skillGenerator = AiServices.builder(SkillGeneratorAgent.class)
                .chatModel(chatModel)
                .build();

        InterviewAgents agents = new InterviewAgents(
                sessionId,
                mainInterviewer,
                viceInterviewer,
                evaluator,
                skillGenerator,
                basicContext,     // 新增：保存基本上下文
                resumeSnapshot,   // 新增：保存简历快照
                jdSnapshot        // 新增：保存 JD 快照
        );

        // 缓存
        agentsCache.put(sessionId, agents);

        log.info("创建面试 Agent 成功（文件化系统提示词），会话ID: {}", sessionId);

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
     * 面试 Agent 组
     */
    public record InterviewAgents(
            String sessionId,
            MainInterviewerAgent mainInterviewer,
            ViceInterviewerAgent viceInterviewer,
            ExpertEvaluatorAgent evaluator,
            SkillGeneratorAgent skillGenerator,
            String basicContext,      // 基本上下文
            String resumeSnapshot,    // 简历快照
            String jdSnapshot         // JD 快照
    ) {
        /**
         * 检查是否所有 Agent 都已创建
         */
        public boolean isReady() {
            return mainInterviewer != null
                    && viceInterviewer != null
                    && evaluator != null
                    && skillGenerator != null;
        }
    }
}
