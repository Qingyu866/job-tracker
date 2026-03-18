package com.jobtracker.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 技能请求
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 2026-03-19
 */
@Data
public class SkillRequest {

    /**
     * 技能ID（关联 skill_tags 表）
     * <p>
     * 前端需要先调用 /api/skills 获取技能列表，用户选择后传入skillId
     * </p>
     */
    @NotNull(message = "技能ID不能为空")
    private Long skillId;

    /**
     * 熟练度：BEGINNER/INTERMEDIATE/ADVANCED/EXPERT
     */
    @NotBlank(message = "熟练度不能为空")
    private String proficiencyLevel;

    /**
     * 使用年限（年）
     */
    private BigDecimal experienceYears;

    /**
     * 最后使用时间
     */
    private LocalDate lastUsedDate;

    /**
     * 是否为核心技能
     */
    private Boolean isCoreSkill;
}
