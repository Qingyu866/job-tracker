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
     * 消息类型常量
     */
    public static final String TYPE_CHAT = "CHAT";
    public static final String TYPE_HEARTBEAT = "HEARTBEAT";
    public static final String TYPE_ERROR = "ERROR";

    /**
     * 消息类型
     * <p>
     * 可选值：
     * - CHAT: 聊天消息
     * - HEARTBEAT: 心跳消息
     * - ERROR: 错误消息
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
                .type(TYPE_CHAT)
                .content(content)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     *创建心跳消息
     *
     * @param content 心跳内容（ping/pong）
     * @return WebSocketMessage 实例
     */
    public static WebSocketMessage heartbeat(String content) {
        return WebSocketMessage.builder()
                .type(TYPE_HEARTBEAT)
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
                .type(TYPE_ERROR)
                .content(content)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 判断是否为心跳消息
     *
     * @return true 如果是 HEARTBEAT 消息
     */
    public boolean isHeartbeat() {
        return TYPE_HEARTBEAT.equals(this.type);
    }
}
