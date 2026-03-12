package com.jobtracker.websocket;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 聊天记忆管理器
 * <p>
 * 为每个 WebSocket 会话管理独立的聊天记忆
 * </p>
 *
 * @author Job Tracker Team
 * @version 1.0.0
 */
@Slf4j
@Component
public class ChatMemoryManager {

    private final ConcurrentHashMap<String, ChatMemory> memories = new ConcurrentHashMap<>();

    /**
     * 获取指定会话的聊天记忆
     *
     * @param sessionId 会话ID
     * @return ChatMemory 实例
     */
    public ChatMemory getMemory(String sessionId) {
        return memories.computeIfAbsent(sessionId, id -> {
            log.info("创建新的聊天记忆：sessionId={}", id);
            return MessageWindowChatMemory.builder()
                    .maxMessages(20)
                    .id(id)
                    .build();
        });
    }

    /**
     * 清除指定会话的聊天记忆
     *
     * @param sessionId 会话ID
     */
    public void clearMemory(String sessionId) {
        memories.remove(sessionId);
        log.info("清除聊天记忆：sessionId={}", sessionId);
    }

    /**
     * 清除所有聊天记忆
     */
    public void clearAll() {
        memories.clear();
        log.info("清除所有聊天记忆");
    }
}
