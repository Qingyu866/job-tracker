package com.jobtracker.agent.memory;

import dev.langchain4j.memory.ChatMemory;
import lombok.Data;

/**
 * 单个模拟面试会话的三个 Agent 记忆容器
 * <p>
 * 确保同一会话内的三个 Agent 记忆互不干扰
 * </p>
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 2026-03-17
 */
@Data
public class SessionMemories {

    /**
     * 模拟面试会话ID
     */
    private final String sessionId;

    /**
     * 主面试官的独立 ChatMemory
     */
    private ChatMemory mainInterviewerMemory;

    /**
     * 副面试官的独立 ChatMemory
     */
    private ChatMemory viceInterviewerMemory;

    /**
     * 评审专家的独立 ChatMemory
     */
    private ChatMemory evaluatorMemory;

    /**
     * 构造函数
     *
     * @param sessionId 模拟面试会话ID
     */
    public SessionMemories(String sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * 根据 AgentType 获取对应的 ChatMemory
     *
     * @param agentType Agent 类型
     * @return 对应的 ChatMemory，可能为 null
     */
    public ChatMemory getMemory(AgentType agentType) {
        return switch (agentType) {
            case MAIN_INTERVIEWER -> mainInterviewerMemory;
            case VICE_INTERVIEWER -> viceInterviewerMemory;
            case EVALUATOR -> evaluatorMemory;
        };
    }

    /**
     * 设置对应 AgentType 的 ChatMemory
     *
     * @param agentType Agent 类型
     * @param memory    ChatMemory 实例
     */
    public void setMemory(AgentType agentType, ChatMemory memory) {
        switch (agentType) {
            case MAIN_INTERVIEWER -> mainInterviewerMemory = memory;
            case VICE_INTERVIEWER -> viceInterviewerMemory = memory;
            case EVALUATOR -> evaluatorMemory = memory;
        }
    }

    /**
     * 检查是否所有 Agent 的记忆都已创建
     *
     * @return 如果三个 ChatMemory 都不为 null，返回 true
     */
    public boolean isComplete() {
        return mainInterviewerMemory != null
                && viceInterviewerMemory != null
                && evaluatorMemory != null;
    }
}
