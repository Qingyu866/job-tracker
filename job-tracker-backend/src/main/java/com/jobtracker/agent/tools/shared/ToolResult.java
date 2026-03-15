package com.jobtracker.agent.tools.shared;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 工具执行结果（统一格式）
 * <p>
 * 所有 AI 工具的返回值都使用此格式，便于：
 * - AI 解析和理解
 * - 前端复用
 * - 统一错误处理
 * </p>
 *
 * @author Job Tracker Team
 * @version 2.0.0
 * @since 2026-03-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ToolResult {

    /** 是否成功 */
    private boolean success;

    /** 消息类型：SUCCESS / ERROR / INFO / MULTIPLE_MATCH */
    private String type;

    /** 消息内容（用户可见） */
    private String message;

    /** 详细数据（可选，AI 可用） */
    private Object data;

    /** 错误代码（失败时） */
    private String errorCode;

    // ========== 静态工厂方法 ==========

    /**
     * 成功结果（仅消息）
     */
    public static ToolResult success(String message) {
        return ToolResult.builder()
                .success(true)
                .type("SUCCESS")
                .message(message)
                .build();
    }

    /**
     * 成功结果（消息 + 数据）
     */
    public static ToolResult success(String message, Object data) {
        return ToolResult.builder()
                .success(true)
                .type("SUCCESS")
                .message(message)
                .data(data)
                .build();
    }

    /**
     * 错误结果（仅消息）
     */
    public static ToolResult error(String message) {
        return ToolResult.builder()
                .success(false)
                .type("ERROR")
                .message(message)
                .build();
    }

    /**
     * 错误结果（错误码 + 消息）
     */
    public static ToolResult error(String errorCode, String message) {
        return ToolResult.builder()
                .success(false)
                .type("ERROR")
                .errorCode(errorCode)
                .message(message)
                .build();
    }

    /**
     * 多匹配结果（需要用户选择）
     */
    public static ToolResult multipleMatch(String message, Object data) {
        return ToolResult.builder()
                .success(false)
                .type("MULTIPLE_MATCH")
                .message(message)
                .data(data)
                .build();
    }

    /**
     * 信息结果（不成功也不失败，如查询结果）
     */
    public static ToolResult info(String message, Object data) {
        return ToolResult.builder()
                .success(true)
                .type("INFO")
                .message(message)
                .data(data)
                .build();
    }

    // ========== 辅助方法 ==========

    /**
     * 判断是否需要用户选择
     */
    public boolean isMultipleMatch() {
        return "MULTIPLE_MATCH".equals(type);
    }

    /**
     * 判断是否为错误
     */
    public boolean isError() {
        return !success && "ERROR".equals(type);
    }
}
