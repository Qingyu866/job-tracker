package com.jobtracker.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 申请日志实体类
 * <p>
 * 对应数据库表：application_logs
 * 存储申请过程中的所有操作日志，用于追踪申请状态变化
 * </p>
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Data
@TableName("application_logs")
public class ApplicationLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 日志ID（主键，自增）
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 申请ID（外键，关联 job_applications 表）
     */
    @TableField("application_id")
    private Long applicationId;

    /**
     * 日志类型（STATUS_CHANGE/INTERVIEW_SCHEDULED/FEEDBACK_RECEIVED/NOTE_ADDED/DOCUMENT_UPLOADED）
     */
    @TableField("log_type")
    private String logType;

    /**
     * 日志标题
     */
    @TableField("log_title")
    private String logTitle;

    /**
     * 日志内容
     */
    @TableField("log_content")
    private String logContent;

    /**
     * 记录者（SYSTEM/USER）
     */
    @TableField("logged_by")
    private String loggedBy;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 逻辑删除标记（0:未删除, 1:已删除）
     */
    @TableLogic
    private Integer deleted;
}
