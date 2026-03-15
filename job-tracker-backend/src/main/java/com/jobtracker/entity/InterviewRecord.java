package com.jobtracker.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 面试记录实体类
 * <p>
 * 对应数据库表：interview_records
 * 存储面试相关的详细信息，包括时间、类型、反馈等
 * 支持多轮面试和面试结果追踪
 * </p>
 *
 * @author Job Tracker Team
 * @version 2.0.0
 * @since 1.0.0
 */
@Data
@TableName("interview_records")
public class InterviewRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 面试记录ID（主键，自增）
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 申请ID（外键，关联 job_applications 表）
     */
    @TableField("application_id")
    private Long applicationId;

    /**
     * 面试类型（PHONE/VIDEO/ONSITE/TECHNICAL/HR）
     */
    @TableField("interview_type")
    private String interviewType;

    /**
     * 面试轮次（第几轮）
     */
    @TableField("round_number")
    private Integer roundNumber;

    /**
     * 是否为终面
     */
    @TableField("is_final")
    private Boolean isFinal;

    /**
     * 面试时间
     */
    @TableField("interview_date")
    private LocalDateTime interviewDate;

    /**
     * 面试官姓名
     */
    @TableField("interviewer_name")
    private String interviewerName;

    /**
     * 面试官职位
     */
    @TableField("interviewer_title")
    private String interviewerTitle;

    /**
     * 面试时长（分钟）
     */
    @TableField("duration_minutes")
    private Integer durationMinutes;

    /**
     * 面试状态
     * SCHEDULED/IN_PROGRESS/COMPLETED/PASSED/FAILED/CANCELLED/NO_SHOW
     */
    private String status;

    /**
     * 面试结果（通过/不通过/待定）
     * 用于 COMPLETED 后记录评估结果
     */
    private String result;

    /**
     * 面试评分（1-5分）
     */
    private Integer rating;

    /**
     * 面试反馈
     */
    private String feedback;

    /**
     * 技术问题记录
     */
    @TableField("technical_questions")
    private String technicalQuestions;

    /**
     * 备注信息
     */
    private String notes;

    /**
     * 是否需要跟进（0:否, 1:是）
     */
    @TableField("follow_up_required")
    private Boolean followUpRequired;

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

    /**
     * 逻辑删除标记（0:未删除, 1:已删除）
     */
    @TableLogic
    private Integer deleted;
}
