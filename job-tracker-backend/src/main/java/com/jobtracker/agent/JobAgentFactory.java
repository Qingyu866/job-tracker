package com.jobtracker.agent;

import com.jobtracker.agent.memory.SafeTurnBasedChatMemoryProvider;
import com.jobtracker.agent.tools.ApplicationTools;
import com.jobtracker.agent.tools.CompanyTools;
import com.jobtracker.agent.tools.InterviewTools;
import com.jobtracker.agent.tools.command.OcrCommandTools;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Job Agent 工厂
 * <p>
 * 动态创建 JobAgent 实例，每个实例使用独立的 ChatMemory
 * 实现真正的会话隔离
 * </p>
 *
 * <h3>使用方式</h3>
 * <pre>
 * // 为特定会话创建 Agent
 * JobAgent agent = jobAgentFactory.createAgent(sessionId);
 * String response = agent.chat(userMessage, currentDate, currentTime, dayOfWeek);
 * </pre>
 *
 * <h3>架构说明</h3>
 * <ul>
 *   <li>直接使用 {@link SafeTurnBasedChatMemoryProvider} 的内部缓存</li>
 *   <li>不需要额外的缓存层，避免重复设计</li>
 *   <li>每个 sessionId 对应独立的 ChatMemory 实例</li>
 * </ul>
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 2026-03-15
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JobAgentFactory {

    private final OpenAiChatModel chatModel;
    private final StreamingChatModel streamingChatModel;
    private final SafeTurnBasedChatMemoryProvider chatMemoryProvider;

    // 工具类（单例，共享使用）
    private final ApplicationTools applicationTools;
    private final InterviewTools interviewTools;
    private final CompanyTools companyTools;
    private final OcrCommandTools ocrCommandTools;

    /**
     * 为指定会话创建 JobAgent
     * <p>
     * 每个会话使用独立的 ChatMemory，实现真正的会话隔离
     * </p>
     *
     * @param sessionId 会话标识
     * @return JobAgent 实例
     */
    public JobAgent createAgent(String sessionId) {
        // 直接从 provider 获取 ChatMemory（内部已有缓存）
        ChatMemory chatMemory = chatMemoryProvider.get(sessionId);

        log.debug("创建 JobAgent：sessionId={}, memoryId={}", sessionId, sessionId);

        // 构建并返回 JobAgent
        return AiServices.builder(JobAgent.class)
                .streamingChatModel(streamingChatModel)
                .chatModel(chatModel)
                .chatMemory(chatMemory)  // 使用独立的 ChatMemory
                // 注册所有工具
                .tools(applicationTools, interviewTools, companyTools, ocrCommandTools)
                .build();
    }

    /**
     * 清除指定会话的记忆
     *
     * @param sessionId 会话标识
     */
    public void clearSession(String sessionId) {
        chatMemoryProvider.clear(sessionId);
        log.info("会话记忆已清除：sessionId={}", sessionId);
    }

    /**
     * 获取当前活跃会话数量
     *
     * @return 活跃会话数
     */
    public int getActiveSessionCount() {
        return chatMemoryProvider.activeSessionCount();
    }
}
