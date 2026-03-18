package com.jobtracker.agent.interview.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 评分详情
 * <p>
 * 用于详细记录各个维度的评分
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
public class ScoreDetail {

    /**
     * 技术准确性（0-4 分）
     * <ul>
     *   <li>概念是否正确</li>
     *   <li>有无明显错误</li>
     *   <li>与简历声称的匹配度</li>
     * </ul>
     */
    private Double technical;

    /**
     * 逻辑清晰度（0-3 分）
     * <ul>
     *   <li>表达是否条理清晰</li>
     *   <li>是否符合 STAR 法则</li>
     * </ul>
     */
    private Double logic;

    /**
     * 深度与广度（0-3 分）
     * <ul>
     *   <li>是否触及底层原理</li>
     *   <li>与工作年限的匹配度</li>
     * </ul>
     */
    private Double depth;
}
