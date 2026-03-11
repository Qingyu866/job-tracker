package com.jobtracker.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 会话管理器
 * <p>
 * 管理 WebSocket 连接会话，支持：
 * - 会话的创建和删除
 * - 根据会话ID查找会话
 * - 向指定会话发送消息
 * - 广播消息到所有会话
 * </p>
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
@Component
public class WebSocketSessionManager {

    /**
     * 会话存储（使用 ConcurrentHashMap 保证线程安全）
     * Key: sessionId, Value: WebSocketSession
     */
    private final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    /**
     * 添加会话
     *
     * @param sessionId 会话ID
     * @param session   WebSocket 会话
     */
    public void addSession(String sessionId, WebSocketSession session) {
        sessions.put(sessionId, session);
        log.info("WebSocket 会话已添加：sessionId={}, 当前连接数={}", sessionId, sessions.size());
    }

    /**
     * 移除会话
     *
     * @param sessionId 会话ID
     */
    public void removeSession(String sessionId) {
        sessions.remove(sessionId);
        log.info("WebSocket 会话已移除：sessionId={}, 当前连接数={}", sessionId, sessions.size());
    }

    /**
     * 获取会话
     *
     * @param sessionId 会话ID
     * @return WebSocket 会话，如果不存在返回 null
     */
    public WebSocketSession getSession(String sessionId) {
        return sessions.get(sessionId);
    }

    /**
     * 检查会话是否存在
     *
     * @param sessionId 会话ID
     * @return 如果存在返回 true，否则返回 false
     */
    public boolean hasSession(String sessionId) {
        return sessions.containsKey(sessionId);
    }

    /**
     * 获取当前连接数
     *
     * @return 连接数
     */
    public int getSessionCount() {
        return sessions.size();
    }

    /**
     * 获取所有会话ID
     *
     * @return 会话ID集合
     */
    public java.util.Set<String> getAllSessionIds() {
        return sessions.keySet();
    }
}
