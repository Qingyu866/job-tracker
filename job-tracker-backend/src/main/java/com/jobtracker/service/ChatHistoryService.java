package com.jobtracker.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobtracker.dto.ChatMessageWithImages;
import com.jobtracker.dto.ImageAttachment;
import com.jobtracker.entity.ChatImage;
import com.jobtracker.entity.ChatMessage;
import com.jobtracker.entity.ChatSession;
import com.jobtracker.entity.ToolCallRecord;
import com.jobtracker.mapper.ChatMessageMapper;
import com.jobtracker.mapper.ChatSessionMapper;
import com.jobtracker.mapper.ToolCallRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 聊天历史服务
 * <p>
 * 负责管理用户与AI的对话历史，包括：
 * - 会话的创建和管理
 * - 消息的保存和查询
 * - 工具调用记录的保存
 * - 图片附件的保存和查询
 * </p>
 *
 * @author Job Tracker Team
 * @version 2.0.0
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatHistoryService {

    private final ChatSessionMapper sessionMapper;
    private final ChatMessageMapper messageMapper;
    private final ToolCallRecordMapper toolCallMapper;
    private final ChatImageService chatImageService;
    private final ObjectMapper objectMapper;

    /**
     * 创建或获取会话
     *
     * @param sessionKey 会话标识
     * @return 会话对象
     */
    public ChatSession getOrCreateSession(String sessionKey) {
        ChatSession session = sessionMapper.selectBySessionKey(sessionKey);
        if (session == null) {
            session = ChatSession.builder()
                    .sessionKey(sessionKey)
                    .messageCount(0)
                    .build();
            sessionMapper.insert(session);
            log.info("创建新会话：sessionKey={}, id={}", sessionKey, session.getId());
        }
        return session;
    }

    /**
     * 保存用户消息
     *
     * @param sessionKey 会话标识
     * @param content    消息内容
     * @param imageIds   图片ID列表（可选，用于关联之前上传的图片）
     * @return 保存的消息对象
     */
    @Transactional(rollbackFor = Exception.class)
    public ChatMessage saveUserMessage(String sessionKey, String content, List<Long> imageIds) {
        ChatSession session = getOrCreateSession(sessionKey);

        ChatMessage message = ChatMessage.builder()
                .sessionId(session.getId())
                .role("USER")
                .content(content)
                .build();
        messageMapper.insert(message);

        // 更新会话消息数
        sessionMapper.incrementMessageCount(session.getId());
        log.debug("保存用户消息：sessionId={}, messageId={}", session.getId(), message.getId());

        // 如果有图片，更新图片的 message_id 关联
        if (imageIds != null && !imageIds.isEmpty()) {
            chatImageService.updateMessageId(imageIds, message.getId());
        }

        return message;
    }

    /**
     * 保存用户消息（不带图片，向后兼容）
     *
     * @param sessionKey 会话标识
     * @param content    消息内容
     * @return 保存的消息对象
     */
    public ChatMessage saveUserMessage(String sessionKey, String content) {
        return saveUserMessage(sessionKey, content, null);
    }

    /**
     * 保存AI消息
     *
     * @param sessionKey 会话标识
     * @param content    消息内容
     * @return 保存的消息对象
     */
    @Transactional(rollbackFor = Exception.class)
    public ChatMessage saveAssistantMessage(String sessionKey, String content) {
        ChatSession session = getOrCreateSession(sessionKey);

        ChatMessage message = ChatMessage.builder()
                .sessionId(session.getId())
                .role("ASSISTANT")
                .content(content)
                .build();
        messageMapper.insert(message);

        // 更新会话消息数
        sessionMapper.incrementMessageCount(session.getId());
        log.debug("保存AI消息：sessionId={}, messageId={}", session.getId(), message.getId());

        return message;
    }

    /**
     * 保存工具调用记录
     *
     * @param messageId        关联的消息ID
     * @param toolName         工具名称
     * @param toolInput        工具入参
     * @param toolOutput       工具输出
     * @param status           状态
     * @param errorMessage     错误信息
     * @param executionTimeMs  执行耗时
     * @return 保存的记录对象
     */
    public ToolCallRecord saveToolCallRecord(Long messageId, String toolName,
                                              Object toolInput, Object toolOutput,
                                              String status, String errorMessage,
                                              Integer executionTimeMs) {
        ToolCallRecord record = ToolCallRecord.builder()
                .messageId(messageId)
                .toolName(toolName)
                .toolInput(toJson(toolInput))
                .toolOutput(toJson(toolOutput))
                .status(status)
                .errorMessage(errorMessage)
                .executionTimeMs(executionTimeMs)
                .build();
        toolCallMapper.insert(record);
        log.debug("保存工具调用记录：messageId={}, toolName={}", messageId, toolName);

        return record;
    }

    /**
     * 获取会话历史消息
     *
     * @param sessionKey 会话标识
     * @return 消息列表
     */
    public List<ChatMessage> getSessionMessages(String sessionKey) {
        ChatSession session = sessionMapper.selectBySessionKey(sessionKey);
        if (session == null) {
            return List.of();
        }
        return messageMapper.selectBySessionId(session.getId());
    }

    /**
     * 获取会话最近的 N 条消息
     * <p>
     * 用于从数据库加载历史消息到 ChatMemory
     * </p>
     *
     * @param sessionKey 会话标识
     * @param limit      限制条数
     * @return 消息列表（按时间升序）
     */
    public List<ChatMessage> getRecentMessages(String sessionKey, int limit) {
        ChatSession session = sessionMapper.selectBySessionKey(sessionKey);
        if (session == null) {
            return List.of();
        }
        return messageMapper.selectRecentMessages(session.getId(), limit);
    }

    /**
     * 获取所有会话列表
     *
     * @return 会话列表
     */
    public List<ChatSession> getAllSessions() {
        return sessionMapper.selectList(null);
    }

    /**
     * 删除会话（逻辑删除）
     *
     * @param sessionKey 会话标识
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteSession(String sessionKey) {
        sessionMapper.deleteBySessionKey(sessionKey);
        log.info("删除会话：sessionKey={}", sessionKey);
    }

    /**
     * 获取消息的工具调用记录
     *
     * @param messageId 消息ID
     * @return 工具调用记录列表
     */
    public List<ToolCallRecord> getToolCallRecords(Long messageId) {
        return toolCallMapper.selectByMessageId(messageId);
    }

    /**
     * 对象转JSON字符串
     *
     * @param obj 对象
     * @return JSON字符串
     */
    private String toJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("JSON序列化失败", e);
            return String.valueOf(obj);
        }
    }

    // ==================== 图片相关方法 ====================

    /**
     * 获取消息关联的图片列表（转换为 DTO）
     *
     * @param messageId  消息ID
     * @param sessionKey 会话标识
     * @return 图片附件列表
     */
    public List<ImageAttachment> getMessageImages(Long messageId, String sessionKey) {
        List<ChatImage> chatImages = chatImageService.getImagesByMessageId(messageId);

        return chatImages.stream()
                .map(img -> ImageAttachment.fromEntity(img, sessionKey))
                .toList();
    }

    /**
     * 获取会话历史消息（含图片信息）
     * <p>
     * 返回的消息列表包含每条消息关联的图片附件
     * </p>
     *
     * @param sessionKey 会话标识
     * @return 消息列表（含图片）
     */
    public List<ChatMessageWithImages> getSessionMessagesWithImages(String sessionKey) {
        ChatSession session = sessionMapper.selectBySessionKey(sessionKey);
        if (session == null) {
            return List.of();
        }

        // 查询消息列表
        List<ChatMessage> messages = messageMapper.selectBySessionId(session.getId());

        // 转换为带图片的消息 DTO
        List<ChatMessageWithImages> result = new ArrayList<>();
        for (ChatMessage message : messages) {
            // 查询消息关联的图片
            List<ChatImage> chatImages = chatImageService.getImagesByMessageId(message.getId());

            // 转换为前端需要的格式
            List<ImageAttachment> imageAttachments = chatImages.stream()
                    .map(img -> ImageAttachment.fromEntity(img, sessionKey))
                    .toList();

            result.add(ChatMessageWithImages.builder()
                    .id(message.getId())
                    .role(message.getRole())
                    .content(message.getContent())
                    .createdAt(message.getCreatedAt())
                    .images(imageAttachments)
                    .build());
        }

        return result;
    }
}
