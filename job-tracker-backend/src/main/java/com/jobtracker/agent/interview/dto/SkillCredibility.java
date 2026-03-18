package com.jobtracker.agent.interview.dto;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.output.structured.Description;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 技能可信度
 * <p>
 * 用于评估单个技能的可信度
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
public class SkillCredibility {

    /**
     * 技能名称
     * <p>
     * 示例："Redis"、"Spring Boot"、"MySQL"
     * </p>
     */
    @Description("技能名称，例如：Redis、Spring Boot、MySQL")
    private String skillName;

    /**
     * 简历声称的熟练度
     * <p>
     * 示例："精通"、"熟练掌握"、"了解"
     * </p>
     */
    @Description("简历中声称的技能熟练度，例如：精通、熟练掌握、了解")
    private String claimedLevel;

    /**
     * 实际表现出的熟练度
     * <p>
     * 示例："精通"、"熟练掌握"、"了解"、"完全不了解"
     * </p>
     */
    @Description("面试中表现出的实际熟练度，例如：精通、熟练掌握、了解、完全不了解")
    private String actualLevel;

    /**
     * 夸大程度
     */
    @Description("简历声称与实际表现的差距程度")
    private ExaggerationLevel exaggerationLevel;

    /**
     * 评价意见
     * <p>
     * 详细说明为什么得出这个结论
     * </p>
     * <p>
     * 示例："简历声称精通 Redis，但对集群模式的回答很基础"
     * </p>
     */
    @Description("详细评价为什么得出这个夸大程度结论，例如：简历声称精通 Redis，但对集群模式的回答很基础")
    private String comment;

    /**
     * 夸大程度枚举
     */
    public enum ExaggerationLevel {
        /**
         * 无夸大
         * <p>
         * 实际表现 ≥ 简历声称
         * </p>
         */
        @Description("无夸大：实际表现达到或超过简历声称")
        NONE,

        /**
         * 轻微夸大
         * <p>
         * 实际表现略低于简历声称，但差距不大
         * </p>
         */
        @Description("轻微夸大：实际表现略低于简历声称，但差距不大")
        SLIGHT,

        /**
         * 中度夸大
         * <p>
         * 实际表现明显低于简历声称
         * </p>
         */
        @Description("中度夸大：实际表现明显低于简历声称")
        MODERATE,

        /**
         * 严重夸大
         * <p>
         * 实际表现远低于简历声称
         * </p>
         */
        @Description("严重夸大：实际表现远低于简历声称，完全不符合")
        SEVERE
    }
}
