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

    // ==================== 进度追踪相关字段（新增）====================

    /**
     * 当前执行的计划ID（用于暂停/恢复）
     */
    private Long currentPlanId;

    /**
     * 总考察计划数量
     */
    private Integer totalPlans;

    /**
     * 已完成计划数量
     */
    private Integer completedPlans;

    /**
     * 暂停时间
     */
    private LocalDateTime pausedAt;

    /**
     * 恢复时间
     */
    private LocalDateTime resumedAt;

    // ==================== 评分结果 ====================

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
     * <p>
     * 定义模拟面试会话在整个生命周期中的所有可能状态
     * </p>
     */
    public enum InterviewState {
        /**
         * 初始化状态
         * <p>
         * 会话刚创建，Agent 组已初始化，但尚未开始面试
         * </p>
         * <p>
         * <b>触发时机</b>: 调用 {@code createSession()} 创建会话后
         * </p>
         * <p>
         * <b>下一步</b>: 用户点击"开始面试"按钮，进入 WELCOME 状态
         * </p>
         */
        INIT,

        /**
         * 欢迎环节
         * <p>
         * 面试官进行自我介绍，说明面试流程，让候选人放松心情
         * </p>
         * <p>
         * <b>触发时机</b>: 用户点击"开始面试"按钮
         * </p>
         * <p>
         * <b>下一步</b>: 主面试官提出第一个问题，进入 TECHNICAL_QA 状态
         * </p>
         */
        WELCOME,

        /**
         * 技术问答环节
         * <p>
         * 主要面试阶段，三个 Agent 协同工作：
         * </p>
         * <ul>
         *   <li><b>主面试官</b>：提出技术问题</li>
         *   <li><b>副面试官</b>：决定下一个考察的知识点</li>
         *   <li><b>评审专家</b>：评估用户回答质量</li>
         * </ul>
         * <p>
         * <b>触发时机</b>: 用户回复欢迎消息后
         * </p>
         * <p>
         * <b>下一步</b>: 达到预设轮次或用户主动结束，进入 FINISHED 状态
         * </p>
         */
        TECHNICAL_QA,

        /**
         * HR问答环节
         * <p>
         * 询问职业规划、薪资期望、离职原因等非技术问题
         * </p>
         * <p>
         * <b>触发时机</b>: 技术问答环节结束
         * </p>
         * <p>
         * <b>当前状态</b>: 暂未使用此状态，所有问答都在 TECHNICAL_QA 中完成
         * </p>
         */
        HR_QA,

        /**
         * 生成报告环节
         * <p>
         * 面试结束后，三个 Agent 协同生成完整的面试报告：
         * </p>
         * <ul>
         *   <li><b>评审专家</b>：生成简历可信度分析和总体评分</li>
         *   <li><b>副面试官</b>：生成改进建议和学习资源推荐</li>
         *   <li><b>主面试官</b>：生成面试总结</li>
         * </ul>
         * <p>
         * <b>触发时机</b>: 用户点击"结束面试"按钮
         * </p>
         * <p>
         * <b>下一步</b>: 报告生成完成后，进入 FINISHED 状态
         * </p>
         */
        GENERATING_REPORT,

        /**
         * 已完成状态
         * <p>
         * 面试已结束，报告已生成，用户可以查看完整报告
         * </p>
         * <p>
         * <b>触发时机</b>: 报告生成完成
         * </p>
         * <p>
         * <b>可操作</b>: 查看报告、重新面试、下载报告等
         * </p>
         */
        FINISHED
    }
}
