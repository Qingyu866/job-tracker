package com.jobtracker.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 工具调用记录实体类
 * <p>
 * 对应数据库表：tool_call_records
 * 存储AI调用工具的详细信息，用于追溯和分析
 * </p>
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("tool_call_records")
public class ToolCallRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 记录ID（主键，自增）
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 关联的消息ID
     */
    @TableField("message_id")
    private Long messageId;

    /**
     * 工具名称
     */
    @TableField("tool_name")
    private String toolName;

    /**
     * 工具入参(JSON)
     */
    @TableField("tool_input")
    private String toolInput;

    /**
     * 工具输出(JSON)
     */
    @TableField("tool_output")
    private String toolOutput;

    /**
     * 状态: SUCCESS/FAILURE
     */
    private String status;

    /**
     * 错误信息
     */
    @TableField("error_message")
    private String errorMessage;

    /**
     * 执行耗时(毫秒)
     */
    @TableField("execution_time_ms")
    private Integer executionTimeMs;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
