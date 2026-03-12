package com.jobtracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WebSocket 消息 DTO
 * <p>
 * 定义 WebSocket 通信的消息格式
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
public class WebSocketMessage {

    /**
     * 消息类型
     * <p>
     * 可选值：
     * - CHAT: 聊天消息
     * - STREAM: 流式响应片段
     * - ERROR: 错误消息
     * - STATUS: 状态通知
     * </p>
     */
    private String type;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 会话ID（用于多会话管理）
     */
    private String sessionId;

    /**
     * 时间戳
     */
    private Long timestamp;

    /**
     * 扩展数据（JSON 格式）
     */
    private Object data;

    /**
     * 创建聊天消息
     *
     * @param content 消息内容
     * @return WebSocketMessage 实例
     */
    public static WebSocketMessage chat(String content) {
        return WebSocketMessage.builder()
                .type("CHAT")
                .content(content)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 创建流式响应片段
     *
     * @param content 消息片段
     * @return WebSocketMessage 实例
     */
    public static WebSocketMessage stream(String content) {
        return WebSocketMessage.builder()
                .type("STREAM")
                .content(content)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 创建错误消息
     *
     * @param content 错误信息
     * @return WebSocketMessage 实例
     */
    public static WebSocketMessage error(String content) {
        return WebSocketMessage.builder()
                .type("ERROR")
                .content(content)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 创建状态通知
     *
     * @param content 状态信息
     * @return WebSocketMessage 实例
     */
    public static WebSocketMessage status(String content) {
        return WebSocketMessage.builder()
                .type("STATUS")
                .content(content)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}
