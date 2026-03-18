package com.jobtracker.dto;

import lombok.Data;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

/**
 * 创建完整简历请求
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 2026-03-19
 */
@Data
public class CreateResumeRequest {

    // ==================== 基本信息（必填） ====================

    /**
     * 简历名称（如"Java后端-3年经验"）
     */
    @NotBlank(message = "简历名称不能为空")
    private String resumeName;

    /**
     * 是否为默认简历
     */
    private Boolean isDefault;

    // ==================== 工作信息（可选） ====================

    /**
     * 工作年限（年）
     */
    private Integer workYears;

    /**
     * 当前职位
     */
    private String currentPosition;

    /**
     * 目标岗位级别：JUNIOR/MIDDLE/SENIOR/LEAD
     */
    private String targetLevel;

    /**
     * 自我介绍
     */
    private String summary;

    // ==================== 关联数据（可选） ====================

    /**
     * 工作经历列表
     */
    @Valid
    private List<WorkExperienceRequest> workExperiences;

    /**
     * 项目经历列表
     */
    @Valid
    private List<ProjectRequest> projects;

    /**
     * 技能列表
     */
    @Valid
    private List<SkillRequest> skills;
}
