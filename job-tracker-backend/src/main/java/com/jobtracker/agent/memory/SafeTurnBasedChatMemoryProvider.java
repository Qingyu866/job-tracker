package com.jobtracker.agent.memory;

import com.jobtracker.entity.ChatMessage;
import com.jobtracker.service.ChatHistoryService;
import dev.langchain4j.data.message.ChatMessageType;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 安全的基于轮次的聊天记忆提供者
 * <p>
 * 功能特性：
 * - 每个会话独立的 ChatMemory
 * - 从数据库加载历史消息
 * - 消息序列验证和修复
 * </p>
 *
 * @author Job Tracker Team
 * @version 2.0.0
 * @since 2026-03-15
 */
@Slf4j
public class SafeTurnBasedChatMemoryProvider implements ChatMemoryProvider {

    private final int maxMessages;
    private final ConcurrentHashMap<Object, SafeTurnBasedChatMemory> memoryStore = new ConcurrentHashMap<>();

    // 注入 ChatHistoryService 用于加载历史消息
    private final ChatHistoryService chatHistoryService;
    private final int historyLoadLimit;  // 加载历史消息的数量

    /**
     * 构造函数
     *
     * @param maxMessages        每个 ChatMemory 最多保留的消息数
     * @param chatHistoryService  聊天历史服务
     * @param historyLoadLimit   从数据库加载的历史消息数量
     */
    public SafeTurnBasedChatMemoryProvider(
            int maxMessages,
            ChatHistoryService chatHistoryService,
            int historyLoadLimit) {
        this.maxMessages = maxMessages;
        this.chatHistoryService = chatHistoryService;
        this.historyLoadLimit = historyLoadLimit;
    }

    @Override
    public ChatMemory get(Object memoryId) {
        // computeIfAbsent：如果不存在则创建，存在则返回缓存的
        return memoryStore.computeIfAbsent(memoryId,
            id -> {
                log.info("创建新的 ChatMemory：memoryId={}", id);

                // 1. 创建空的 ChatMemory
                SafeTurnBasedChatMemory memory = new SafeTurnBasedChatMemory(id, maxMessages);

                // 2. 从数据库加载历史消息
                if (chatHistoryService != null && id instanceof String) {
                    String sessionKey = (String) id;
                    loadHistoryFromDatabase(memory, sessionKey);
                }

                return memory;
            });
    }

    /**
     * 从数据库加载历史消息到 ChatMemory
     *
     * @param memory     ChatMemory 实例
     * @param sessionKey 会话标识
     */
    private void loadHistoryFromDatabase(SafeTurnBasedChatMemory memory, String sessionKey) {
        try {
            // 查询最近的消息
            List<ChatMessage> dbMessages =
                chatHistoryService.getRecentMessages(sessionKey, historyLoadLimit);

            if (dbMessages.isEmpty()) {
                log.debug("没有历史消息需要加载：sessionKey={}", sessionKey);
                return;
            }

            // 转换并添加到 ChatMemory
            for (ChatMessage dbMsg : dbMessages) {
                dev.langchain4j.data.message.ChatMessage chatMessage = convertToChatMessage(dbMsg);
                memory.add(chatMessage);
            }

            log.info("已加载历史消息：sessionKey={}, count={}", sessionKey, dbMessages.size());

            // ⚠️ 关键：验证并修复消息序列
            ensureMessageOrderCorrect(memory);

        } catch (Exception e) {
            log.error("加载历史消息失败：sessionKey={}", sessionKey, e);
            // 失败不影响会话创建，只是没有历史而已
        }
    }

    /**
     * 修复消息顺序，避免 "Conversation roles must alternate" 错误
     * <p>
     * 修复策略：
     * 1. SYSTEM 锚定：若存在 SYSTEM，以其为起点，之前的全部丢弃（解决 USER 在 SYSTEM 前的问题）
     * 2. 严格交替：SYSTEM 后 USER/ASSISTANT 必须交替，SYSTEM 本身重置交替状态
     * 3. 结尾完整：删除末尾的 USER（避免未完成的请求发送给模型）
     * </p>
     *
     * @param memory ChatMemory 实例
     */
    private void ensureMessageOrderCorrect(SafeTurnBasedChatMemory memory) {
        List<dev.langchain4j.data.message.ChatMessage> messages = memory.getMessages();

        if (messages == null || messages.isEmpty()) {
            return;
        }

        int originalSize = messages.size();
        List<dev.langchain4j.data.message.ChatMessage> fixed = new ArrayList<>(originalSize);

        // 1. 【关键改进】预扫描：找到第一个 SYSTEM 的位置
        // 如果存在 SYSTEM，它必须是有效对话的起点，之前的全部视为脏数据
        int firstSystemIndex = -1;
        for (int i = 0; i < messages.size(); i++) {
            if (messages.get(i).type() == ChatMessageType.SYSTEM) {
                firstSystemIndex = i;
                break;
            }
        }

        // 确定遍历的起始位置
        int startIndex = (firstSystemIndex != -1) ? firstSystemIndex : 0;

        // 状态变量
        boolean firstUserFound = false;   // 仅在无 SYSTEM 模式下需要
        ChatMessageType lastType = null;  // 上一条非 SYSTEM 消息的类型
        int lastUserIndex = -1;           // fixed 列表中最后一条 USER 的索引

        // 2. 从确定的起点开始遍历
        for (int i = startIndex; i < messages.size(); i++) {
            dev.langchain4j.data.message.ChatMessage msg = messages.get(i);
            ChatMessageType type = msg.type();

            // --- 处理 SYSTEM 消息 ---
            if (type == ChatMessageType.SYSTEM) {
                fixed.add(msg);
                lastType = null;  // 🔑 关键修复：SYSTEM 作为分隔符，重置交替状态
                // 注意：如果是第一个 SYSTEM (i==startIndex)，它自然成为列表第一个
                // 如果是中间的 SYSTEM，它也会重置 lastType，允许后续接 USER 或 ASSISTANT
                continue;
            }

            // --- 处理无 SYSTEM 模式下的开头 ---
            // 只有在没有 SYSTEM 消息时，才需要寻找第一条 USER 作为起点
            if (firstSystemIndex == -1 && !firstUserFound) {
                if (type == ChatMessageType.USER) {
                    firstUserFound = true;
                    fixed.add(msg);
                    lastType = type;
                    lastUserIndex = fixed.size() - 1;
                }
                // 跳过开头的 ASSISTANT/TOOL
                continue;
            }

            // --- 处理严格交替 (USER/ASSISTANT) ---
            // 此时 firstUserFound 必然为 true (要么有 SYSTEM，要么已找到 USER)
            if (type == lastType) {
                // 发现连续同类型 (如 USER->USER)，跳过以修复交替错误
                continue;
            }

            // 添加有效消息
            fixed.add(msg);
            lastType = type;

            if (type == ChatMessageType.USER) {
                lastUserIndex = fixed.size() - 1;
            }
        }

        // 3. 【结尾修复】确保不以 USER 结尾
        // 如果最后一条是 USER，说明用户发完消息还没得到回答，发送给模型会报错
        if (lastUserIndex >= 0 && lastUserIndex == fixed.size() - 1) {
            fixed.remove(lastUserIndex);
        }

        // 4. 更新内存
        messages.clear();
        messages.addAll(fixed);

        int fixedSize = messages.size();
        if (originalSize != fixedSize) {
            log.info("消息序列修复：memoryId={}, 原始={}, 修复={}, 移除={}",
                     memory.id(), originalSize, fixedSize, originalSize - fixedSize);
        }
    }

    /**
     * 将数据库消息转换为 LangChain4j ChatMessage
     *
     * @param dbMsg 数据库消息实体
     * @return LangChain4j ChatMessage
     */
    private dev.langchain4j.data.message.ChatMessage convertToChatMessage(ChatMessage dbMsg) {
        String role = dbMsg.getRole();
        String content = dbMsg.getContent();

        return switch (role.toUpperCase()) {
            case "USER" -> dev.langchain4j.data.message.UserMessage.from(content);
            case "ASSISTANT" -> dev.langchain4j.data.message.AiMessage.from(content);
            case "SYSTEM" -> dev.langchain4j.data.message.SystemMessage.from(content);
            default -> throw new IllegalArgumentException("Unknown role: " + role);
        };
    }

    /**
     * 手动清除某个会话的记忆
     *
     * @param memoryId 会话ID
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
     *
     * @return 活跃会话数
     */
    public int activeSessionCount() {
        return memoryStore.size();
    }
}
