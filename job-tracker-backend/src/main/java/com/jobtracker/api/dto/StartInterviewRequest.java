package com.jobtracker.api.dto;

import lombok.Data;

/**
 * 开始面试请求
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 2026-03-16
 */
@Data
public class StartInterviewRequest {
    /**
     * 申请ID
     */
    private Long applicationId;

    /**
     * 用户ID
     */
    private Long userId;
}
