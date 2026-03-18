package com.jobtracker.agent.interview.dto;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 简历真实性评估
 * <p>
 * 用于评估候选人回答与简历声称的匹配度
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
public class CredibilityAssessment {

    /**
     * 匹配度等级
     */
    @Description("回答与简历声称的匹配度等级")
    private MatchLevel matchLevel;

    /**
     * 差距描述
     * <p>
     * 详细说明简历声称与实际表现的差距
     * </p>
     * <p>
     * 示例："简历声称精通 Redis，但对集群模式的回答很基础"
     * </p>
     */
    @Description("详细说明简历声称与实际表现的差距")
    private String gapDescription;

    /**
     * 夸大程度评分（0-1）
     * <ul>
     *   <li>0.0-0.2: 完全匹配或超出预期</li>
     *   <li>0.3-0.5: 轻微夸大</li>
     *   <li>0.6-0.8: 部分夸大</li>
     *   <li>0.9-1.0: 严重夸大</li>
     * </ul>
     */
    @Description("夸大程度评分，范围0.0-1.0。0.0-0.2表示完全匹配，0.3-0.5表示轻微夸大，0.6-0.8表示部分夸大，0.9-1.0表示严重夸大")
    private Double exaggerationScore;

    /**
     * 匹配度等级枚举
     */
    public enum MatchLevel {
        /**
         * 完全匹配
         * <p>
         * 回答与简历声称一致，甚至超出预期
         * </p>
         */
        @Description("完全匹配：回答与简历声称一致，甚至超出预期")
        PERFECT_MATCH,

        /**
         * 基本匹配
         * <p>
         * 回答与简历声称基本一致，可能有些许夸大
         * </p>
         */
        @Description("基本匹配：回答与简历声称基本一致，可能有些许夸大")
        MOSTLY_MATCH,

        /**
         * 部分夸大
         * <p>
         * 回答有一定基础，但未达到简历声称的水平
         * </p>
         */
        @Description("部分夸大：回答有一定基础，但未达到简历声称的水平")
        PARTIALLY_EXAGGERATED,

        /**
         * 严重夸大
         * <p>
         * 回答明显低于简历声称的水平
         * </p>
         */
        @Description("严重夸大：回答明显低于简历声称的水平")
        SEVERELY_EXAGGERATED,

        /**
         * 超出预期
         * <p>
         * 回答超出简历声称的水平
         * </p>
         */
        @Description("超出预期：回答超出简历声称的水平")
        EXCEEDS_EXPECTATIONS
    }
}
