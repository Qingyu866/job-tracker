package com.jobtracker.agent.interview.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 总体可信度评分结果
 * <p>
 * 用于表示整体简历的可信度评分
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
public class CredibilityScoreResult {

    /**
     * 可信度评分（0-1）
     * <ul>
     *   <li>0.9-1.0: 高度可信</li>
     *   <li>0.7-0.8: 基本可信</li>
     *   <li>0.5-0.6: 部分可疑</li>
     *   <li>0.3-0.4: 高度可疑</li>
     *   <li>0.0-0.2: 完全不可信</li>
     * </ul>
     */
    private Double credibilityScore;

    /**
     * 评分原因
     * <p>
     * 详细说明评分依据
     * </p>
     */
    private String reason;
}
