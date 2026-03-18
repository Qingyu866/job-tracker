package com.jobtracker.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 简历项目经历实体
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 2026-03-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("resume_projects")
public class ResumeProject {

    /**
     * 项目ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long projectId;

    /**
     * 简历ID
     */
    private Long resumeId;

    /**
     * 项目名称
     */
    private String projectName;

    /**
     * 担任角色
     */
    private String role;

    /**
     * 开始时间
     */
    private LocalDate startDate;

    /**
     * 结束时间（NULL表示至今）
     */
    private LocalDate endDate;

    /**
     * 是否进行中
     */
    private Boolean isOngoing;

    // 项目描述

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

    // 技术栈（重要！用于生成针对性问题）

    /**
     * 技术栈：["Java", "Spring Boot", "MySQL"]
     */
    private String techStack;

    // 项目指标（重要！用于深入挖掘）

    /**
     * 项目规模：团队人数
     */
    private String projectScale;

    /**
     * 性能指标：{"qps": "10000", "response_time": "50ms"}
     */
    private String performanceMetrics;

    /**
     * 显示顺序
     */
    private Integer displayOrder;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
