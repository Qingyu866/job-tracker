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
    @TableId(type = IdType.ASSIGN_ID)
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
     * <p>
     * 定义候选人对某个技能的掌握程度，用于面试时确定问题难度
     * </p>
     * <p>
     * <b>影响</b>:
     * </p>
     * <ul>
     *   <li>问题深度：熟练度越高，问题越深入</li>
     *   <li>验证重点：高熟练度会触发更严格的真实性验证</li>
     *   <li>评分标准：熟练度越高，评分标准越严格</li>
     * </ul>
     */
    public enum ProficiencyLevel {
        /**
         * 初学者/了解
         * <p>
         * <b>定义</b>: 听说过该技能，了解基本概念，但缺乏实际使用经验
         * </p>
         * <p>
         * <b>面试策略</b>:
         * </p>
         * <ul>
         *   <li>提问难度：1-2分（5分制）</li>
         *   <li>问题类型：基本概念、简单用法</li>
         *   <li>验证重点：验证是否真的了解基础</li>
         * </ul>
         * <p>
         * <b>示例问题</b>:
         * </p>
         * <ul>
         *   <li>"你听说过 Redis 吗？它是什么？"</li>
         *   <li>"HashMap 和 HashTable 有什么区别？"</li>
         * </ul>
         * <p>
         * <b>简历可信度评估</b>:
         * </p>
         * <ul>
         *   <li>如果回答正确 → 符合预期</li>
         *   <li>如果回答错误 → 可能是简历夸大</li>
         * </ul>
         */
        BEGINNER,

        /**
         * 中级/熟悉
         * <p>
         * <b>定义</b>: 有实际使用经验，能够在指导下完成常见任务
         * </p>
         * <p>
         * <b>面试策略</b>:
         * </p>
         * <ul>
         *   <li>提问难度：2-3分（5分制）</li>
         *   <li>问题类型：实际应用、常见问题</li>
         *   <li>验证重点：验证是否真的用过</li>
         * </ul>
         * <p>
         * <b>示例问题</b>:
         * </p>
         * <ul>
         *   <li>"Redis 的持久化方式有哪些？你用过哪种？"</li>
         *   <li>"HashMap 在高并发下会出现什么问题？如何解决？"</li>
         * </ul>
         * <p>
         * <b>简历可信度评估</b>:
         * </p>
         * <ul>
         *   <li>如果回答深入 → 可能谦虚了</li>
         *   <li>如果回答基础 → 符合预期</li>
         *   <li>如果回答错误 → 简历夸大</li>
         * </ul>
         */
        INTERMEDIATE,

        /**
         * 高级/精通
         * <p>
         * <b>定义</b>: 能够独立使用该技能解决复杂问题，了解底层原理
         * </p>
         * <p>
         * <b>面试策略</b>:
         * </p>
         * <ul>
         *   <li>提问难度：3-4分（5分制）</li>
         *   <li>问题类型：底层原理、性能优化、源码分析</li>
         *   <li>验证重点：<b>重点验证真实性</b>（防止简历夸大）</li>
         * </ul>
         * <p>
         * <b>示例问题</b>:
         * </p>
         * <ul>
         *   <li>"Redis Cluster 的 Slot 迁移机制是怎样的？"</li>
         *   <li>"HashMap 在 Java 7 和 Java 8 的实现有什么不同？"</li>
         *   <li>"如何排查 Redis 的内存泄漏问题？"</li>
         * </ul>
         * <p>
         * <b>简历可信度评估</b>:
         * </p>
         * <ul>
         *   <li>如果回答深入 → 完全匹配</li>
         *   <li>如果回答基础 → <b>部分夸大</b></li>
         *   <li>如果回答错误 → <b>严重夸大</b></li>
         * </ul>
         */
        ADVANCED,

        /**
         * 专家/权威
         * <p>
         * <b>定义</b>: 该领域的权威，能够进行技术决策，影响技术发展方向
         * </p>
         * <p>
         * <b>面试策略</b>:
         * </p>
         * <ul>
         *   <li>提问难度：4-5分（5分制）</li>
         *   <li>问题类型：架构设计、技术选型、前沿技术</li>
         *   <li>验证重点：<b>极其严格的真实性验证</b></li>
         * </ul>
         * <p>
         * <b>示例问题</b>:
         * </p>
         * <ul>
         *   <li>"如果让你重新设计 Redis，你会怎么改进？"</li>
         *   <li>"你如何看待 NoSQL 和关系型数据库的未来发展？"</li>
         *   <li>"你在该领域有哪些开源贡献或技术博客？"</li>
         * </ul>
         * <p>
         * <b>简历可信度评估</b>:
         * </p>
         * <ul>
         *   <li>如果回答有深度 → 可能是真的专家</li>
         *   <li>如果回答一般 → <b>严重夸大</b></li>
         *   <li>需要提供证明：开源项目、技术文章、专利等</li>
         * </ul>
         */
        EXPERT
    }
}
