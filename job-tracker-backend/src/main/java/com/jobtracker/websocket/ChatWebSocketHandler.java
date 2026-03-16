package com.jobtracker.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobtracker.agent.JobAgent;
import com.jobtracker.agent.JobAgentFactory;
import com.jobtracker.agent.MultimodalJobAgent;
import com.jobtracker.dto.ImageAttachment;
import com.jobtracker.dto.WebSocketMessage;
import com.jobtracker.entity.ChatImage;
import com.jobtracker.entity.ChatMessage;
import com.jobtracker.service.ChatHistoryService;
import com.jobtracker.service.ChatImageService;
import com.jobtracker.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;

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
    private final JobAgentFactory jobAgentFactory;  // 使用工厂模式，支持动态创建 Agent
    private final MultimodalJobAgent multimodalJobAgent;  // 多模态 Agent（支持图片）
    private final ChatHistoryService chatHistoryService;
    private final ChatImageService chatImageService;
    private final FileStorageService fileStorageService;  // 用于读取图片文件
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
            List<Long> imageIds = wsMessage.getImageIds();

            log.info("解析消息：sessionId={}, type={}, content={}, imageIds={}",
                    sessionId, messageType, content, imageIds != null ? imageIds.size() : 0);

            // 处理心跳消息（不调用 AI）- 支持大小写不敏感
            if ("HEARTBEAT".equalsIgnoreCase(messageType)) {
                log.info("收到心跳消息：sessionId={}", sessionId);
                WebSocketMessage pongMessage = WebSocketMessage.heartbeat("pong");
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(pongMessage)));
                return;
            }

            // 验证：内容和图片不能同时为空
            boolean hasContent = content != null && !content.trim().isEmpty();
            boolean hasImages = imageIds != null && !imageIds.isEmpty();

            if (!hasContent && !hasImages) {
                WebSocketMessage errorMessage = WebSocketMessage.error("消息内容和图片不能同时为空");
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(errorMessage)));
                return;
            }

            // 使用前端传入的sessionId作为会话标识，如果未提供则使用WebSocket的sessionId
            String chatSessionKey = wsMessage.getSessionId() != null ? wsMessage.getSessionId() : sessionId;

            // 1. 保存用户消息（传入 imageIds 用于关联）
            ChatMessage savedMessage = chatHistoryService.saveUserMessage(chatSessionKey, content, imageIds);

            // 2. 查询图片信息（用于 AI 调用）
            List<ImageAttachment> images = null;
            if (hasImages) {
                images = imageIds.stream()
                        .map(chatImageService::getById)
                        .filter(img -> img != null)
                        .map(img -> ImageAttachment.fromEntity(img, chatSessionKey))
                        .toList();
                log.debug("查询到图片附件：count={}", images.size());
            }

            // 3. 调用 AI Agent（传入当前时间）
            log.info("调用 AI Agent：sessionKey={}, userMessage={}, hasImages={}",
                    chatSessionKey, content, hasImages);

            // 构建当前时间参数
            LocalDateTime now = LocalDateTime.now();
            String currentDate = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String currentTime = now.format(DateTimeFormatter.ofPattern("HH:mm"));
            String dayOfWeek = getChineseDayOfWeek(now.toLocalDate().getDayOfWeek());

            String aiResponse;

            if (hasImages && images != null && !images.isEmpty()) {
                // 多模态调用：使用 MultimodalJobAgent 直接调用 LM Studio API
                aiResponse = multimodalJobAgent.chatWithImages(content, images, currentDate, currentTime, dayOfWeek);
            } else {
                // 纯文本调用：使用 JobAgentFactory 创建的 Agent（支持工具调用）
                JobAgent jobAgent = jobAgentFactory.createAgent(chatSessionKey);
                aiResponse = jobAgent.chat(content, currentDate, currentTime, dayOfWeek);
            }

            log.info("AI 响应：sessionKey={}, response={}", chatSessionKey, aiResponse);

            // 4. 保存AI消息
            chatHistoryService.saveAssistantMessage(chatSessionKey, aiResponse);
            log.debug("AI消息已保存：sessionKey={}", chatSessionKey);

            // 5. 发送响应
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

    /**
     * 获取中文星期几
     */
    private String getChineseDayOfWeek(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> "星期一";
            case TUESDAY -> "星期二";
            case WEDNESDAY -> "星期三";
            case THURSDAY -> "星期四";
            case FRIDAY -> "星期五";
            case SATURDAY -> "星期六";
            case SUNDAY -> "星期日";
        };
    }
}
