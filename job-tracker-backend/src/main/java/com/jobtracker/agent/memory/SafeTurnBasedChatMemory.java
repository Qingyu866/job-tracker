package com.jobtracker.agent.memory;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageType;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;


public class SafeTurnBasedChatMemory implements ChatMemory {

    private final Object id;
    private final int maxMessages;
    private final List<ChatMessage> messages;

    public SafeTurnBasedChatMemory(Object id, int maxMessages) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.maxMessages = Math.max(4, maxMessages); // 建议至少留 4 条，防止一轮对话都放不下
        this.messages = new ArrayList<>();
    }



    @Override
    public Object id() {
        return id;
    }

    @Override
    public void add(ChatMessage message) {
        if (message instanceof SystemMessage) {
            if (messages.isEmpty() || !(messages.get(0) instanceof SystemMessage)) {
                messages.add(0, message);
            } else {
                messages.set(0, message);
            }
            return;
        }
        messages.add(message);
        evictExcessMessages();
    }

    private void evictExcessMessages() {
        //Step 1: 计算当前对话消息数量
        int systemCount = (!messages.isEmpty() && messages.get(0) instanceof SystemMessage) ? 1 : 0;
        int conversationCount = messages.size() - systemCount;

        if (conversationCount <= maxMessages) {
            return;  // 没超限，直接返回，不用移除
        }

        //Step 2: 找到"最旧一轮"的起始位置（第一个 User）
        int firstUserIndex = -1;
        for (int i = systemCount; i < messages.size(); i++) {
            if (messages.get(i).type() == ChatMessageType.USER) {
                firstUserIndex = i;
                break;  // 找到第一个 User 就停
            }
        }

        //Step 3: 极端情况处理（没有 User 消息)
        if (firstUserIndex == -1) {
            // 极端情况：没有 User 消息，只有 Assistant/Tool，全部清空
            messages.subList(systemCount, messages.size()).clear();
            return;
        }

        //Step 4: 找到"下一轮"的起始位置（下一个 User）
        int nextUserIndex = -1;
        for (int i = firstUserIndex + 1; i < messages.size(); i++) {
            if (messages.get(i).type() == ChatMessageType.USER) {
                nextUserIndex = i;
                break;
            }
        }

        // Step 5: 执行"整轮移除"
        int removeEndIndex = (nextUserIndex != -1) ? nextUserIndex : messages.size();
        messages.subList(firstUserIndex, removeEndIndex).clear();

        //Step 6: 二次兜底 - 确保 System 后是 User
        if (messages.size() > systemCount) {
            ChatMessage firstConvMsg = messages.get(systemCount);
            // 如果第一条不是 User，继续删！
            while (messages.size() > systemCount &&
                    messages.get(systemCount).type() != ChatMessageType.USER) {
                messages.remove(systemCount);
            }
        }

        //Step 7: 三次兜底 - 防止连续 Use
        sanitizeConsecutiveUsers();  // 调用下面的辅助方法
    }

    private void sanitizeConsecutiveUsers() {
        // 从后往前检查，如果有连续 User，保留最后一个，删除前面的
        for (int i = messages.size() - 1; i > 0; i--) {
            if (messages.get(i).type() == ChatMessageType.USER &&
                    messages.get(i-1).type() == ChatMessageType.USER) {
                messages.remove(i-1);  // 删除前一个 User
                i--;  // ⚠️ 关键：删除后索引要回退，避免跳过检查
            }
        }
    }

    @Override
    public List<ChatMessage> messages() {
        return new ArrayList<>(messages);
    }

    @Override
    public void clear() {
        messages.clear();
    }
}
