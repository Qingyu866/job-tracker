package com.jobtracker.agent.interview.dto;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

/**
 * 改进建议
 * <p>
 * 用于在面试结束时提供针对性的改进建议
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
public class ImprovementSuggestion {

    /**
     * 建议类别
     * <p>
     * 示例："技术深度"、"项目经验"、"沟通表达"
     * </p>
     */
    @Description("建议类别，例如：技术深度、项目经验、沟通表达")
    private String category;

    /**
     * 建议标题
     * <p>
     * 简洁概括建议内容
     * </p>
     * <p>
     * 示例："加强 Redis 集群原理学习"
     * </p>
     */
    @Description("建议标题，简洁概括建议内容，例如：加强 Redis 集群原理学习")
    private String title;

    /**
     * 建议描述
     * <p>
     * 详细说明为什么需要改进以及如何改进
     * </p>
     */
    @Description("详细说明为什么需要改进以及如何改进")
    private String description;

    /**
     * 学习资源列表
     * <p>
     * 推荐的学习资源、书籍、课程等
     * </p>
     */
    @Description("推荐的学习资源、书籍、课程等列表")
    private List<String> resources;

    /**
     * 优先级
     */
    @Description("改进的优先级")
    private Priority priority;

    /**
     * 优先级枚举
     */
    public enum Priority {
        /**
         * 高优先级
         * <p>
         * 严重影响面试结果，需要立即改进
         * </p>
         */
        @Description("高优先级：严重影响面试结果，需要立即改进")
        HIGH,

        /**
         * 中优先级
         * <p>
         * 有一定影响，建议在短期内改进
         * </p>
         */
        @Description("中优先级：有一定影响，建议在短期内改进")
        MEDIUM,

        /**
         * 低优先级
         * <p>
         * 影响较小，可以长期培养
         * </p>
         */
        @Description("低优先级：影响较小，可以长期培养")
        LOW
    }
}
