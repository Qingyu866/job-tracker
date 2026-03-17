package com.jobtracker.agent.memory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobtracker.entity.InterviewMemory;
import com.jobtracker.mapper.InterviewMemoryMapper;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 面试记忆提供者
 * <p>
 * 实现两层隔离：
 * <ul>
 *   <li><b>会话间隔离</b>：不同 sessionId 的记忆完全独立</li>
 *   <li><b>Agent 间隔离</b>：同一会话内三个 Agent 的记忆完全独立</li>
 * </ul>
 * </p>
 * <p>
 * 隔离机制：
 * <ul>
 *   <li>内存层：Map&lt;sessionId, SessionMemories&gt; 确保会话隔离</li>
 *   <li>Agent层：SessionMemories 内部包含三个独立的 ChatMemory</li>
 *   <li>持久化层：数据库中 session_id UNIQUE 约束确保物理隔离</li>
 * </ul>
 * </p>
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 2026-03-17
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InterviewMemoryProvider {

    private final InterviewMemoryMapper memoryMapper;
    private final ObjectMapper objectMapper;

    /**
     * L1 内存缓存：sessionId → SessionMemories
     * <p>
     * 确保不同会话的记忆在内存中完全隔离
     * </p>
     */
    private final Map<String, SessionMemories> memoryCache = new ConcurrentHashMap<>();

    /**
     * 获取指定会话和 Agent 类型的 ChatMemory
     * <p>
     * 关键方法：确保 (sessionId, agentType) 组合有独立的 ChatMemory 实例
     * </p>
     *
     * @param sessionId 模拟面试会话ID（来自 MockInterviewSession.sessionId）
     * @param agentType Agent类型
     * @return 独立的 ChatMemory 实例
     */
    public ChatMemory getChatMemory(String sessionId, AgentType agentType) {
        // 1. 获取或创建会话的 SessionMemories（会话隔离）
        SessionMemories sessionMemories = memoryCache.computeIfAbsent(sessionId, id -> {
            log.info("创建新会话记忆容器，会话ID: {}", id);
            return loadOrCreateSessionMemories(id);
        });

        // 2. 获取或创建指定 Agent 的 ChatMemory（Agent 隔离）
        ChatMemory memory = sessionMemories.getMemory(agentType);
        if (memory == null) {
            memory = createChatMemory(sessionId, agentType);
            sessionMemories.setMemory(agentType, memory);
            log.info("创建 {} ChatMemory，会话ID: {}", agentType.getDisplayName(), sessionId);
        }

        return memory;
    }

    /**
     * 从数据库加载或创建新的会话记忆
     *
     * @param sessionId 模拟面试会话ID
     * @return SessionMemories 实例
     */
    private SessionMemories loadOrCreateSessionMemories(String sessionId) {
        // 先从数据库加载
        InterviewMemory dbMemory = memoryMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<InterviewMemory>()
                        .eq(InterviewMemory::getSessionId, sessionId)
        );

        if (dbMemory == null) {
            // 不存在则创建新记录
            dbMemory = new InterviewMemory();
            dbMemory.setSessionId(sessionId);
            memoryMapper.insert(dbMemory);
            log.info("创建数据库记忆记录，会话ID: {}", sessionId);
        } else {
            log.info("从数据库加载记忆记录，会话ID: {}", sessionId);
        }

        // 创建 SessionMemories 容器
        SessionMemories sessionMemories = new SessionMemories(sessionId);

        // 从数据库恢复三个 Agent 的 ChatMemory
        sessionMemories.setMainInterviewerMemory(
                restoreChatMemory(dbMemory.getMainInterviewerMemory())
        );
        sessionMemories.setViceInterviewerMemory(
                restoreChatMemory(dbMemory.getViceInterviewerMemory())
        );
        sessionMemories.setEvaluatorMemory(
                restoreChatMemory(dbMemory.getEvaluatorMemory())
        );

        return sessionMemories;
    }

    /**
     * 创建新的 ChatMemory 实例
     * <p>
     * 每个调用都返回全新的实例，确保完全隔离
     * </p>
     *
     * @param sessionId 模拟面试会话ID
     * @param agentType Agent 类型
     * @return 新的 ChatMemory 实例
     */
    private ChatMemory createChatMemory(String sessionId, AgentType agentType) {
        // 使用 sessionId:agentType 作为唯一 ID
        String memoryId = sessionId + ":" + agentType.getCode();

        return MessageWindowChatMemory.builder()
                .maxMessages(50)  // 保留最近 50 条消息
                .id(memoryId)     // 唯一 ID 确保隔离
                .build();
    }

    /**
     * 从 JSON 恢复 ChatMemory
     *
     * @param json JSON 字符串
     * @return ChatMemory 实例，如果解析失败返回 null
     */
    private ChatMemory restoreChatMemory(String json) {
        if (json == null || json.isBlank() || "null".equals(json)) {
            return null;
        }

        try {
            // 解析 JSON，恢复消息列表
            List<MessageDto> messages = objectMapper.readValue(
                    json,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, MessageDto.class)
            );

            // 构建新的 ChatMemory
            ChatMemory memory = MessageWindowChatMemory.builder()
                    .maxMessages(50)
                    .build();

            for (MessageDto msg : messages) {
                if ("user".equals(msg.role)) {
                    memory.add(UserMessage.from(msg.content));
                } else if ("ai".equals(msg.role)) {
                    memory.add(AiMessage.from(msg.content));
                }
            }

            log.debug("恢复 ChatMemory 成功，消息数: {}", messages.size());
            return memory;
        } catch (JsonProcessingException e) {
            log.error("恢复 ChatMemory 失败", e);
            return null;
        }
    }

    /**
     * 持久化指定 Agent 的记忆到数据库
     *
     * @param sessionId 模拟面试会话ID
     * @param agentType Agent 类型
     */
    public void persistMemory(String sessionId, AgentType agentType) {
        SessionMemories sessionMemories = memoryCache.get(sessionId);
        if (sessionMemories == null) {
            log.warn("会话记忆不存在，无法持久化: {}", sessionId);
            return;
        }

        ChatMemory memory = sessionMemories.getMemory(agentType);
        if (memory == null) {
            log.warn("{} 记忆不存在，无法持久化: {}", agentType.getDisplayName(), sessionId);
            return;
        }

        // 转换为 JSON
        String json = convertToJson(memory);

        // 更新数据库
        InterviewMemory dbMemory = memoryMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<InterviewMemory>()
                        .eq(InterviewMemory::getSessionId, sessionId)
        );

        if (dbMemory != null) {
            switch (agentType) {
                case MAIN_INTERVIEWER -> dbMemory.setMainInterviewerMemory(json);
                case VICE_INTERVIEWER -> dbMemory.setViceInterviewerMemory(json);
                case EVALUATOR -> dbMemory.setEvaluatorMemory(json);
            }
            memoryMapper.updateById(dbMemory);
            log.debug("持久化 {} 记忆成功，会话ID: {}", agentType.getDisplayName(), sessionId);
        } else {
            log.warn("数据库中不存在记忆记录，会话ID: {}", sessionId);
        }
    }

    /**
     * 持久化会话的所有 Agent 记忆
     *
     * @param sessionId 模拟面试会话ID
     */
    public void persistAll(String sessionId) {
        persistMemory(sessionId, AgentType.MAIN_INTERVIEWER);
        persistMemory(sessionId, AgentType.VICE_INTERVIEWER);
        persistMemory(sessionId, AgentType.EVALUATOR);
        log.info("持久化会话所有记忆成功，会话ID: {}", sessionId);
    }

    /**
     * 将 ChatMemory 转换为 JSON
     *
     * @param memory ChatMemory 实例
     * @return JSON 字符串
     */
    private String convertToJson(ChatMemory memory) {
        try {
            List<MessageDto> messages = new ArrayList<>();
            for (ChatMessage msg : memory.messages()) {
                if (msg instanceof UserMessage) {
                    messages.add(new MessageDto("user", ((UserMessage) msg).singleText()));
                } else if (msg instanceof AiMessage) {
                    AiMessage aiMsg = (AiMessage) msg;
                    messages.add(new MessageDto("ai", aiMsg.text()));
                }
            }
            return objectMapper.writeValueAsString(messages);
        } catch (JsonProcessingException e) {
            log.error("转换 ChatMemory 为 JSON 失败", e);
            return "[]";
        }
    }

    /**
     * 清除会话记忆（从内存缓存中移除）
     * <p>
     * 注意：此操作仅从内存中移除，数据库记录保留
     * </p>
     *
     * @param sessionId 模拟面试会话ID
     */
    public void clearMemory(String sessionId) {
        memoryCache.remove(sessionId);
        log.info("清除会话记忆缓存，会话ID: {}", sessionId);
    }

    /**
     * 完全删除会话记忆（包括数据库记录）
     *
     * @param sessionId 模拟面试会话ID
     */
    public void deleteMemory(String sessionId) {
        memoryCache.remove(sessionId);
        memoryMapper.deleteBySessionId(sessionId);
        log.info("删除会话记忆（包括数据库），会话ID: {}", sessionId);
    }

    /**
     * 获取会话的 InterviewMemory 实体
     *
     * @param sessionId 模拟面试会话ID
     * @return InterviewMemory 实例，如果不存在返回 null
     */
    public InterviewMemory getInterviewMemory(String sessionId) {
        return memoryMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<InterviewMemory>()
                        .eq(InterviewMemory::getSessionId, sessionId)
        );
    }

    /**
     * 消息 DTO（用于 JSON 序列化）
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    private static class MessageDto {
        private String role;
        private String content;
    }
}
