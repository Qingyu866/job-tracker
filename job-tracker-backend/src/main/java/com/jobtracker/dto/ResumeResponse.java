package com.jobtracker.dto;

import com.jobtracker.entity.ResumeProject;
import com.jobtracker.entity.ResumeSkill;
import com.jobtracker.entity.ResumeWorkExperience;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 完整简历响应
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 2026-03-19
 */
@Data
@Builder
public class ResumeResponse {

    // ==================== 基本信息 ====================

    /**
     * 简历ID
     */
    private Long resumeId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 简历名称
     */
    private String resumeName;

    /**
     * 是否为默认简历
     */
    private Boolean isDefault;

    /**
     * 工作年限
     */
    private Integer workYears;

    /**
     * 当前职位
     */
    private String currentPosition;

    /**
     * 目标岗位级别
     */
    private String targetLevel;

    /**
     * 自我介绍
     */
    private String summary;

    // ==================== 关联数据 ====================

    /**
     * 工作经历列表
     */
    private List<ResumeWorkExperience> workExperiences;

    /**
     * 项目经历列表
     */
    private List<ResumeProject> projects;

    /**
     * 技能列表
     */
    private List<ResumeSkill> skills;

    // ==================== 时间戳 ====================

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
