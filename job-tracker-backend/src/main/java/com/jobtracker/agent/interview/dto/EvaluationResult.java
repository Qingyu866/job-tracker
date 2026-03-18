package com.jobtracker.agent.interview.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 评审专家评估结果
 * <p>
 * 用于评审专家 Agent 评估候选人的回答
 * </p>
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 2026-03-18
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationResult {

    /**
     * 评分详情
     */
    private ScoreDetail scores;

    /**
     * 总分（0-10）
     */
    private Double totalScore;

    /**
     * 简历真实性评估
     */
    private CredibilityAssessment credibilityAssessment;

    /**
     * 反馈意见
     */
    private String feedback;

    /**
     * 改进建议
     */
    private String suggestion;
}
