package com.jobtracker.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 模拟面试配置
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 2026-03-21
 */
@Data
@Component
@ConfigurationProperties(prefix = "mock-interview")
public class MockInterviewConfig {

    /**
     * 面试总轮数
     */
    private Integer totalRounds = 25;

    /**
     * 每轮最大问题数（1个问题 + 可能的追问）
     */
    private Integer maxQuestionsPerRound = 1;

    /**
     * 是否启用追问功能
     */
    private Boolean enableFollowup = false;

    /**
     * 最少轮数（不允许提前结束）
     */
    private Integer minRounds = 3;

    /**
     * 建议的最大时长（分钟）
     */
    private Integer suggestedDurationMinutes = 45;
}
