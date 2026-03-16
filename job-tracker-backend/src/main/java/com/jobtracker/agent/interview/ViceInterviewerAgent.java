package com.jobtracker.agent.interview;

/**
 * 副面试官 Agent
 * <p>
 * 职责：选题、去重、状态流转
 * 重点：平衡 JD 要求 vs 简历内容
 * </p>
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 2026-03-16
 */
public interface ViceInterviewerAgent {

    /**
     * 决定下一个步骤
     *
     * @param context 上下文信息（包含 JD、简历、已考察知识点、用户最新回答等）
     * @return 下一步决策（JSON 格式）
     */
    String decideNextStep(String context);
}
