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
 * 申请-技能关联实体
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 2026-03-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("application_skill_mapping")
public class ApplicationSkillMapping {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 申请ID
     */
    private Long applicationId;

    /**
     * 技能ID
     */
    private Long skillId;

    /**
     * 优先级：1-核心, 2-重要, 3-了解
     */
    private Integer priority;

    /**
     * 来源：MANUAL/JD_AI/PARSED
     */
    private String source;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 优先级枚举
     */
    public enum Priority {
        /**
         * 核心技能
         */
        CORE(1),

        /**
         * 重要技能
         */
        IMPORTANT(2),

        /**
         * 了解即可
         */
        NICE_TO_HAVE(3);

        private final int value;

        Priority(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    /**
     * 来源枚举
     */
    public enum Source {
        /**
         * 手动添加
         */
        MANUAL,

        /**
         * JD AI 解析
         */
        JD_AI,

        /**
         * 简历解析
         */
        PARSED
    }
}
