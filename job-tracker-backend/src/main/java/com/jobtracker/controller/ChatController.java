package com.jobtracker.controller;

import com.jobtracker.common.result.Result;
import com.jobtracker.entity.ChatMessage;
import com.jobtracker.entity.ChatSession;
import com.jobtracker.entity.ToolCallRecord;
import com.jobtracker.service.ChatHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 聊天历史API控制器
 * <p>
 * 提供聊天会话和消息历史的管理接口
 * </p>
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatHistoryService chatHistoryService;

    /**
     * 获取所有会话列表
     *
     * @return 会话列表
     */
    @GetMapping("/sessions")
    public Result<List<ChatSession>> getAllSessions() {
        List<ChatSession> sessions = chatHistoryService.getAllSessions();
        return Result.success("查询成功", sessions);
    }

    /**
     * 获取会话消息历史
     *
     * @param sessionKey 会话标识
     * @return 消息列表
     */
    @GetMapping("/sessions/{sessionKey}/messages")
    public Result<List<ChatMessage>> getSessionMessages(@PathVariable String sessionKey) {
        List<ChatMessage> messages = chatHistoryService.getSessionMessages(sessionKey);
        return Result.success("查询成功", messages);
    }

    /**
     * 删除会话
     *
     * @param sessionKey 会话标识
     * @return 删除结果
     */
    @DeleteMapping("/sessions/{sessionKey}")
    public Result<Boolean> deleteSession(@PathVariable String sessionKey) {
        chatHistoryService.deleteSession(sessionKey);
        return Result.success("删除成功", true);
    }

    /**
     * 获取消息的工具调用记录
     *
     * @param messageId 消息ID
     * @return 工具调用记录列表
     */
    @GetMapping("/messages/{messageId}/tool-calls")
    public Result<List<ToolCallRecord>> getToolCallRecords(@PathVariable Long messageId) {
        List<ToolCallRecord> records = chatHistoryService.getToolCallRecords(messageId);
        return Result.success("查询成功", records);
    }
}
