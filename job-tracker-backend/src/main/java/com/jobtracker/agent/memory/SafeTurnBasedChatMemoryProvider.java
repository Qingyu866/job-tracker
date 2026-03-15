package com.jobtracker.agent.memory;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class SafeTurnBasedChatMemoryProvider implements ChatMemoryProvider {

    private final int maxMessages;
    private final ConcurrentHashMap<Object, SafeTurnBasedChatMemory> memoryStore = new ConcurrentHashMap<>();

    public SafeTurnBasedChatMemoryProvider(int maxMessages) {
        this.maxMessages = maxMessages;
    }

    @Override
    public ChatMemory get(Object memoryId) {
        // 计算如果不存在则创建，存在则返回缓存的
        return memoryStore.computeIfAbsent(memoryId, 
            id -> new SafeTurnBasedChatMemory(id, maxMessages));
    }

    /**
     * 手动清除某个会话的记忆
     */
    public void clear(Object memoryId) {
        SafeTurnBasedChatMemory memory = memoryStore.remove(memoryId);
        if (memory != null) {
            memory.clear();
        }
    }

    /**
     * 清除所有会话
     */
    public void clearAll() {
        memoryStore.values().forEach(ChatMemory::clear);
        memoryStore.clear();
    }

    /**
     * 获取当前活跃会话数（监控用）
     */
    public int activeSessionCount() {
        return memoryStore.size();
    }
}