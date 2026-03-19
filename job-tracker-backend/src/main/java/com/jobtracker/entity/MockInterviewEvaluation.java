package com.jobtracker.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 面试评分记录实体
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 2026-03-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("mock_interview_evaluations")
public class MockInterviewEvaluation {

    /**
     * 评分ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long evaluationId;

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 轮次号
     */
    private Integer roundNumber;

    /**
     * 知识点ID
     */
    private Long skillId;

    /**
     * 知识点名称（冗余，便于查询）
     */
    private String skillName;

    // ==================== 考察计划相关字段（新增）====================

    /**
     * 计划状态：PENDING(待考察), IN_PROGRESS(执行中), COMPLETED(已完成), SKIPPED(跳过), PAUSED(暂停中)
     */
    private String planStatus;

    /**
     * 选题来源：PROJECT_DEEP_DIVE, SKILL_VERIFICATION, JD_REQUIREMENT, GENERAL
     */
    private String topicSource;

    /**
     * 问题类型：PROJECT_DEEP_DIVE, SKILL_VERIFICATION, JD_REQUIREMENT, GENERAL, OPEN_ENDED
     */
    private String questionType;

    /**
     * 计划难度（1-5）
     */
    private Integer plannedDifficulty;

    /**
     * 实际难度（可能与计划不同）
     */
    private Integer actualDifficulty;

    /**
     * 上下文信息（例如：简历声称精通Redis）
     */
    private String contextInfo;

    /**
     * 选择该技能的原因
     */
    private String reason;

    // ==================== 面试执行字段 ====================

    /**
     * 问题内容（AI生成的问题）
     */
    private String questionText;

    /**
     * 用户回答
     */
    private String userAnswer;

    // 评分明细

    /**
     * 技术准确性得分（0-4）
     */
    private BigDecimal technicalScore;

    /**
     * 逻辑清晰度得分（0-3）
     */
    private BigDecimal logicScore;

    /**
     * 深度与广度得分（0-3）
     */
    private BigDecimal depthScore;

    /**
     * 总分（0-10）
     */
    private BigDecimal totalScore;

    /**
     * 详细反馈
     */
    private String feedback;

    /**
     * 改进建议
     */
    private String suggestion;

    /**
     * 匹配的关键词
     */
    private String keywordsMatched;

    /**
     * 缺失的关键词
     */
    private String keywordsMissing;

    /**
     * 评分时间
     */
    private LocalDateTime evaluatedAt;
}
