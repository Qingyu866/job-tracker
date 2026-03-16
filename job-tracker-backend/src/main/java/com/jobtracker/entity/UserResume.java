package com.jobtracker.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户简历实体（极简版）
 * <p>
 * 模拟面试只关注技术能力，不需要隐私信息
 * </p>
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 2026-03-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_resumes")
public class UserResume {

    /**
     * 简历ID
     */
    @TableId(type = IdType.AUTO)
    private Long resumeId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 简历名称（如"Java后端-3年经验"）
     */
    private String resumeName;

    /**
     * 是否为默认简历
     */
    private Boolean isDefault;

    // 工作信息（影响面试问题难度）

    /**
     * 工作年限（年）- 影响：3年问JVM，1年问基础
     */
    private Integer workYears;

    /**
     * 当前职位 - 如：Java后端工程师
     */
    private String currentPosition;

    /**
     * 目标岗位级别：JUNIOR/MIDDLE/SENIOR
     */
    private String targetLevel;

    // 自我介绍（AI 用于了解技术背景）

    /**
     * 自我介绍（突出技术栈、项目经验）
     */
    private String summary;

    // 原始文件（用于 AI 解析）

    /**
     * 原始简历文件URL
     */
    private String originalFileUrl;

    /**
     * 文件类型：PDF/DOCX/IMG
     */
    private String originalFileType;

    // 解析状态

    /**
     * 解析状态：PENDING/PARSING/SUCCESS/FAILED
     */
    private String parseStatus;

    /**
     * 解析完成时间
     */
    private LocalDateTime parsedAt;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 目标岗位级别枚举
     */
    public enum TargetLevel {
        /**
         * 初级
         */
        JUNIOR,

        /**
         * 中级
         */
        MIDDLE,

        /**
         * 高级
         */
        SENIOR,

        /**
         * 专家
         */
        LEAD
    }

    /**
     * 解析状态枚举
     */
    public enum ParseStatus {
        /**
         * 待解析
         */
        PENDING,

        /**
         * 解析中
         */
        PARSING,

        /**
         * 解析成功
         */
        SUCCESS,

        /**
         * 解析失败
         */
        FAILED
    }
}
