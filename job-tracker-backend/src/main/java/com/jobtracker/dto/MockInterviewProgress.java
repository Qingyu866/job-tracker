package com.jobtracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 模拟面试进度 DTO
 * <p>
 * 用于返回模拟面试会话的当前进度信息
 * </p>
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 2026-03-19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MockInterviewProgress {
    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 会话状态（PREPARING/TECHNICAL_QA/PAUSED/COMPLETED）
     */
    private String state;

    /**
     * 当前轮次
     */
    private Integer currentRound;

    /**
     * 总计划数
     */
    private Integer totalPlans;

    /**
     * 已完成计划数
     */
    private Integer completedPlans;

    /**
     * 待执行计划数
     */
    private Integer pendingPlans;

    /**
     * 完成进度百分比（0-100）
     */
    private Double progressPercentage;

    /**
     * 暂停时间
     */
    private String pausedAt;

    /**
     * 恢复时间
     */
    private String resumedAt;
}
