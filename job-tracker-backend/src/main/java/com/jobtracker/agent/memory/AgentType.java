package com.jobtracker.agent.memory;

/**
 * 面试 Agent 类型枚举
 * <p>
 * 用于区分模拟面试系统中的三个不同角色
 * </p>
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 2026-03-17
 */
public enum AgentType {

    /**
     * 主面试官
     * <p>
     * 职责：根据副面试官选定的主题，向用户提出面试问题
     * </p>
     */
    MAIN_INTERVIEWER("main", "主面试官"),

    /**
     * 副面试官（选题控制）
     * <p>
     * 职责：分析用户回答，决定下一个要考察的知识点
     * </p>
     */
    VICE_INTERVIEWER("vice", "副面试官"),

    /**
     * 评审专家
     * <p>
     * 职责：评估用户的回答质量，分析简历真实性
     * </p>
     */
    EVALUATOR("evaluator", "评审专家");

    private final String code;
    private final String displayName;

    AgentType(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }
}
