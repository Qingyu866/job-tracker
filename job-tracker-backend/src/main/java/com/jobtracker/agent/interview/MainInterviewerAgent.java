package com.jobtracker.agent.interview;

/**
 * 主面试官 Agent
 * <p>
 * 职责：基于 JD + 简历进行提问，深度挖掘项目经历和声称技能
 * </p>
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 2026-03-16
 */
public interface MainInterviewerAgent {

    /**
     * 根据上下文提问
     *
     * @param context 上下文信息（包含 JD、简历、已考察知识点等）
     * @return 生成的问题
     */
    String askQuestion(String context);
}
