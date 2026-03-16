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
 * 简历工作经历实体
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 2026-03-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("resume_work_experiences")
public class ResumeWorkExperience {

    /**
     * 工作经历ID
     */
    @TableId(type = IdType.AUTO)
    private Long experienceId;

    /**
     * 简历ID
     */
    private Long resumeId;

    /**
     * 公司名称
     */
    private String companyName;

    /**
     * 职位
     */
    private String position;

    /**
     * 开始时间
     */
    private LocalDate startDate;

    /**
     * 结束时间
     */
    private LocalDate endDate;

    /**
     * 是否为当前公司
     */
    private Boolean isCurrent;

    // 工作描述

    /**
     * 工作描述
     */
    private String description;

    /**
     * 工作成就
     */
    private String achievements;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
