package com.jobtracker.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 面试记忆实体
 * <p>
 * 存储模拟面试会话中三个 Agent 的独立记忆
 * </p>
 * <p>
 * 两层隔离：
 * <ul>
 *   <li>会话间隔离：通过 session_id 区分不同的模拟面试会话</li>
 *   <li>Agent 间隔离：通过三个独立的 JSON 字段存储不同 Agent 的记忆</li>
 * </ul>
 * </p>
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 2026-03-17
 */
@Data
@TableName("interview_memories")
public class InterviewMemory implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 记忆ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 模拟面试会话ID
     * <p>
     * 对应 {@link com.jobtracker.entity.MockInterviewSession#getSessionId()}
     * </p>
     */
    @TableField("session_id")
    private String sessionId;

    /**
     * 主面试官记忆（JSON）
     * <p>
     * 存储结构示例：
     * <pre>
     * {
     *   "questions_asked": [...],
     *   "user_responses": [...],
     *   "questioning_strategy": {...}
     * }
     * </pre>
     * </p>
     */
    @TableField("main_interviewer_memory")
    private String mainInterviewerMemory;

    /**
     * 副面试官记忆（JSON）
     * <p>
     * 存储结构示例：
     * <pre>
     * {
     *   "topics_covered": [...],
     *   "topics_pending": [...],
     *   "decision_history": [...]
     * }
     * </pre>
     * </p>
     */
    @TableField("vice_interviewer_memory")
    private String viceInterviewerMemory;

    /**
     * 评审专家记忆（JSON）
     * <p>
     * 存储结构示例：
     * <pre>
     * {
     *   "scoring_history": [...],
     *   "credibility_analysis": {...},
     *   "improvement_suggestions": [...]
     * }
     * </pre>
     * </p>
     */
    @TableField("evaluator_memory")
    private String evaluatorMemory;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
