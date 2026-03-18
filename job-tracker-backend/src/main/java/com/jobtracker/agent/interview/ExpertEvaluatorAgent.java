package com.jobtracker.agent.interview;

import com.jobtracker.agent.interview.dto.EvaluationResult;
import com.jobtracker.agent.interview.dto.SkillCredibility;
import com.jobtracker.agent.interview.dto.CredibilityScoreResult;

import java.util.List;

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
     * @return 评估结果对象（LangChain4j 会自动处理序列化）
     */
    EvaluationResult evaluate(String context);

    /**
     * 生成简历可信度分析报告
     * <p>
     * 在面试结束时调用，分析整个面试过程中的所有回答
     * 对比简历声称与实际表现，生成可信度分析
     * </p>
     *
     * @param context 完整的面试上下文（包含所有对话记录、简历快照、JD要求等）
     * @return 技能可信度列表（LangChain4j 会自动处理序列化）
     */
    List<SkillCredibility> generateCredibilityAnalysis(String context);

    /**
     * 计算总体可信度评分
     * <p>
     * 基于所有技能的可信度分析，计算一个 0-1 之间的总体评分
     * </p>
     *
     * @param context 完整的面试上下文
     * @return 总体可信度评分对象（LangChain4j 会自动处理序列化）
     */
    CredibilityScoreResult calculateCredibilityScore(String context);
}
