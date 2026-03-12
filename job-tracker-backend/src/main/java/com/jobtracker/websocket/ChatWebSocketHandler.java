package com.jobtracker.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobtracker.agent.JobAgent;
import com.jobtracker.dto.WebSocketMessage;
import dev.langchain4j.service.AiServices;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * WebSocket 聊天处理器
 * <p>
 * 处理 WebSocket 连接和消息：
 * - 连接建立和关闭
 * - 接收客户端消息
 * - 调用 AI Agent 生成响应
 * - 流式返回 AI 响应
 * </p>
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final WebSocketSessionManager sessionManager;
    private final JobAgent jobAgent;
    private final ObjectMapper objectMapper;

    /**
     * 连接建立后调用
     *
     * @param session WebSocket 会话
     * @throws Exception 异常
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = session.getId();
        sessionManager.addSession(sessionId, session);
        log.info("WebSocket 连接已建立：sessionId={}", sessionId);

        // 发送欢迎消息（使用 chat 类型，前端会自动显示）
        WebSocketMessage welcomeMessage = WebSocketMessage.chat("连接成功，您现在可以开始对话了");
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(welcomeMessage)));
    }

    /**
     * 接收到客户端消息时调用
     *
     * @param session WebSocket 会话
     * @param message 文本消息
     * @throws Exception 异常
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String sessionId = session.getId();
        String payload = message.getPayload();

        log.info("收到消息：sessionId={}, type=unknown, payload={}", sessionId, payload);

        try {
            // 解析消息
            WebSocketMessage wsMessage = objectMapper.readValue(payload, WebSocketMessage.class);
            String messageType = wsMessage.getType();
            String content = wsMessage.getContent();

            log.info("解析消息：sessionId={}, type={}, content={}", sessionId, messageType, content);

            // 处理心跳消息（不调用 AI）- 支持大小写不敏感
            if ("HEARTBEAT".equalsIgnoreCase(messageType)) {
                log.info("收到心跳消息：sessionId={}", sessionId);
                WebSocketMessage pongMessage = WebSocketMessage.heartbeat("pong");
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(pongMessage)));
                return;
            }

            // 处理聊天消息
            if (content == null || content.trim().isEmpty()) {
                WebSocketMessage errorMessage = WebSocketMessage.error("消息内容不能为空");
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(errorMessage)));
                return;
            }

            // 调用 AI Agent
            log.info("调用 AI Agent：sessionId={}, userMessage={}", sessionId, content);

            String aiResponse = jobAgent.chat(content);
            log.info("AI 响应：sessionId={}, response={}", sessionId, aiResponse);

            // 发送响应
            WebSocketMessage responseMessage = WebSocketMessage.chat(aiResponse);
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(responseMessage)));

        } catch (Exception e) {
            log.error("处理消息失败：sessionId={}", sessionId, e);
            WebSocketMessage errorMessage = WebSocketMessage.error("处理失败：" + e.getMessage());
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(errorMessage)));
        }
    }

    /**
     * 连接关闭后调用
     *
     * @param session WebSocket 会话
     * @param status  关闭状态
     * @throws Exception 异常
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String sessionId = session.getId();
        sessionManager.removeSession(sessionId);
        log.info("WebSocket 连接已关闭：sessionId={}, status={}", sessionId, status);
    }

    /**
     * 传输错误时调用
     *
     * @param session   WebSocket 会话
     * @param exception 异常
     * @throws Exception 异常
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        String sessionId = session.getId();
        log.error("WebSocket 传输错误：sessionId={}", sessionId, exception);

        if (session.isOpen()) {
            WebSocketMessage errorMessage = WebSocketMessage.error("连接错误：" + exception.getMessage());
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(errorMessage)));
        }
    }
}
