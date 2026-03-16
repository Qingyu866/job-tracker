package com.jobtracker.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 简历技能实体
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 2026-03-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("resume_skills")
public class ResumeSkill {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 简历ID
     */
    private Long resumeId;

    /**
     * 技能ID（关联 skill_tags）
     */
    private Long skillId;

    /**
     * 熟练度：BEGINNER/INTERMEDIATE/ADVANCED/EXPERT
     */
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

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 熟练度枚举
     */
    public enum ProficiencyLevel {
        /**
         * 初学者
         */
        BEGINNER,

        /**
         * 中级
         */
        INTERMEDIATE,

        /**
         * 高级
         */
        ADVANCED,

        /**
         * 专家
         */
        EXPERT
    }
}
