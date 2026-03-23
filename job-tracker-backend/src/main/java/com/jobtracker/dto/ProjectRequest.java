package com.jobtracker.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

/**
 * 项目经历请求
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 2026-03-19
 */
@Data
public class ProjectRequest {

    /**
     * 项目名称
     */
    @NotBlank(message = "项目名称不能为空")
    private String projectName;

    /**
     * 担任角色
     */
    private String role;

    /**
     * 开始时间
     */
    @NotNull(message = "项目开始时间不能为空")
    private LocalDate startDate;

    /**
     * 结束时间（null表示至今）
     */
    private LocalDate endDate;

    /**
     * 是否进行中
     */
    private Boolean isOngoing;

    /**
     * 项目描述
     */
    private String description;

    /**
     * 职责描述
     */
    private String responsibilities;

    /**
     * 项目成就
     */
    private String achievements;

    /**
     * 技术栈（JSON数组字符串：["Java", "Spring Boot"]）
     */
    private List<String> techStack;

    /**
     * 项目规模
     */
    private String projectScale;

    /**
     * 性能指标
     */
    private String performanceMetrics;

    /**
     * 显示顺序
     */
    private Integer displayOrder;
}
