package com.jobtracker.agent.interview;

/**
 * 评审专家 Agent
 * <p>
 * 职责：评分 + 对比简历声称与实际表现的差距
 * </p>
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 2026-03-16
 */
public interface ExpertEvaluatorAgent {

    /**
     * 评估用户回答
     *
     * @param context 评估上下文（包含问题、回答、简历声称、JD要求等）
     * @return 评估结果（JSON 格式，包含分数、反馈、简历真实性分析）
     */
    String evaluate(String context);
}
