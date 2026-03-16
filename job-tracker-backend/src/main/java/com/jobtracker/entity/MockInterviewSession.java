package com.jobtracker.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 模拟面试会话实体
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 2026-03-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("mock_interview_sessions")
public class MockInterviewSession {

    /**
     * 会话ID
     */
    @TableId
    private String sessionId;

    /**
     * 关联的求职申请ID
     */
    private Long applicationId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 关联的简历ID
     */
    private Long resumeId;

    // 岗位信息（冗余，便于查询）

    /**
     * 公司ID
     */
    private Long companyId;

    /**
     * 岗位名称
     */
    private String jobTitle;

    /**
     * 岗位级别
     */
    private String seniorityLevel;

    // 快照（重要！避免后续更新影响历史记录）

    /**
     * 简历快照
     */
    private String resumeSnapshot;

    /**
     * JD 技能快照
     */
    private String jdSnapshot;

    /**
     * 技能标签快照（⚠️ 重要！保存技能当时的名称，避免 skill_tags 表更新影响历史）
     */
    private String skillsSnapshot;

    // 面试状态

    /**
     * 当前状态：INIT/WELCOME/TECHNICAL_QA/HR_QA/GENERATING_REPORT/FINISHED
     */
    private String state;

    /**
     * 当前轮次
     */
    private Integer currentRound;

    /**
     * 总轮次（可调整）
     */
    private Integer totalRounds;

    // 面试进度

    /**
     * 已考察的知识点
     */
    private String skillsCovered;

    /**
     * 待考察的知识点
     */
    private String skillsPending;

    /**
     * 已讨论的项目
     */
    private String projectsDiscussed;

    // 评分结果

    /**
     * 最终总分
     */
    private BigDecimal totalScore;

    /**
     * 面试总结
     */
    private String feedbackSummary;

    /**
     * 改进建议
     */
    private String improvementSuggestions;

    // 简历真实性分析

    /**
     * 简历可信度评分（0-1）
     */
    private BigDecimal resumeCredibilityScore;

    /**
     * 简历差距分析
     */
    private String resumeGapAnalysis;

    // 时间

    /**
     * 开始时间
     */
    private LocalDateTime startedAt;

    /**
     * 结束时间
     */
    private LocalDateTime finishedAt;

    /**
     * 持续时间（秒）
     */
    private Integer durationSeconds;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 面试状态枚举
     */
    public enum InterviewState {
        /**
         * 初始化
         */
        INIT,

        /**
         * 欢迎环节
         */
        WELCOME,

        /**
         * 技术问答
         */
        TECHNICAL_QA,

        /**
         * HR问答
         */
        HR_QA,

        /**
         * 生成报告
         */
        GENERATING_REPORT,

        /**
         * 已完成
         */
        FINISHED
    }
}
