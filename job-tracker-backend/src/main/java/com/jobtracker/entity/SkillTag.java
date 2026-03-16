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
 * 技能标签实体
 * <p>
 * 存储知识点（如：HashMap、Redis 集群、Spring Boot 自动装配）
 * 作为"待考察知识点清单"，支持层级结构
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
@TableName("skill_tags")
public class SkillTag {

    /**
     * 技能ID
     */
    @TableId(type = IdType.AUTO)
    private Long skillId;

    /**
     * 技能名称：Java, HashMap, Spring Boot等
     */
    private String skillName;

    /**
     * 分类：LANGUAGE/FRAMEWORK/DATABASE/TOOL/ALGORITHM
     */
    private String category;

    /**
     * 父技能ID（支持层级结构：Java → Collection → HashMap）
     */
    private Long parentId;

    /**
     * 技能描述
     */
    private String description;

    /**
     * 基础难度：1-5
     */
    private Integer difficultyBase;

    /**
     * 热度分数（根据使用频率更新）
     */
    private Integer hotScore;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 技能分类枚举
     */
    public enum Category {
        /**
         * 编程语言
         */
        LANGUAGE,

        /**
         * 框架
         */
        FRAMEWORK,

        /**
         * 数据库
         */
        DATABASE,

        /**
         * 工具
         */
        TOOL,

        /**
         * 算法与数据结构
         */
        ALGORITHM
    }
}
