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
    @TableId(type = IdType.AUTO)
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
