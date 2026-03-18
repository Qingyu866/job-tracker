package com.jobtracker.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * 工作经历请求
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 2026-03-19
 */
@Data
public class WorkExperienceRequest {

    /**
     * 公司名称
     */
    @NotBlank(message = "公司名称不能为空")
    private String companyName;

    /**
     * 职位
     */
    @NotBlank(message = "职位不能为空")
    private String position;

    /**
     * 开始时间
     */
    @NotNull(message = "开始时间不能为空")
    private LocalDate startDate;

    /**
     * 结束时间（null表示至今）
     */
    private LocalDate endDate;

    /**
     * 是否为当前公司
     */
    private Boolean isCurrent;

    /**
     * 工作描述
     */
    private String description;

    /**
     * 工作成就
     */
    private String achievements;
}
