package com.jobtracker.agent.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobtracker.entity.ToolCallRecord;
import com.jobtracker.mapper.ChatMessageMapper;
import com.jobtracker.mapper.ToolCallRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 工具调用记录辅助类
 * <p>
 * 由于 Spring AOP 与 LangChain4j 不兼容（代理类导致 @Tool 注解丢失），
 * 需要在工具方法中手动调用此记录器来追踪工具调用
 * </p>
 *
 * <h3>使用方法</h3>
 * <pre>
 * // 在工具方法中
 * long startTime = System.currentTimeMillis();
 * try {
 *     // 执行业务逻辑
 *     ToolResult result = doSomething();
 *     // 记录成功调用
 *     ToolCallTrace.record("工具名称", params, result, startTime);
 *     return result;
 * } catch (Exception e) {
 *     // 记录失败调用
 *     ToolCallTrace.recordFailure("工具名称", params, e, startTime);
 *     throw e;
 * }
 * </pre>
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ToolCallTrace {

    private static ToolCallRecordMapper toolCallRecordMapper;
    private static ChatMessageMapper chatMessageMapper;
    private static ObjectMapper objectMapper;

    /**
     * Spring 依赖注入（静态方法需要）
     */
    public ToolCallTrace(ToolCallRecordMapper toolCallRecordMapper,
                         ChatMessageMapper chatMessageMapper,
                         ObjectMapper objectMapper) {
        ToolCallTrace.toolCallRecordMapper = toolCallRecordMapper;
        ToolCallTrace.chatMessageMapper = chatMessageMapper;
        ToolCallTrace.objectMapper = objectMapper;
    }

    /**
     * 获取当前 AI 消息 ID
     * <p>
     * 通过查询最新的一条 ASSISTANT 消息来获取
     * </p>
     */
    private static Long getCurrentAssistantMessageId() {
        try {
            // 查询最新的一条 ASSISTANT 消息
            return chatMessageMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.jobtracker.entity.ChatMessage>()
                    .eq(com.jobtracker.entity.ChatMessage::getRole, "ASSISTANT")
                    .orderByDesc(com.jobtracker.entity.ChatMessage::getId)
                    .last("LIMIT 1")
            ).getId();
        } catch (Exception e) {
            log.debug("无法获取当前消息ID", e);
            return null;
        }
    }

    /**
     * 记录工具调用（成功）
     *
     * @param toolName         工具名称
     * @param toolInput        工具输入参数
     * @param toolOutput       工具输出结果
     * @param startTimeMs      开始时间（毫秒）
     */
    public static void record(String toolName, Object toolInput, Object toolOutput, long startTimeMs) {
        Long messageId = getCurrentAssistantMessageId();
        if (messageId == null) {
            log.debug("无消息ID上下文，跳过工具调用记录：toolName={}", toolName);
            return;
        }

        try {
            long executionTimeMs = System.currentTimeMillis() - startTimeMs;

            ToolCallRecord record = ToolCallRecord.builder()
                    .messageId(messageId)
                    .toolName(toolName)
                    .toolInput(toJson(toolInput))
                    .toolOutput(toJson(toolOutput))
                    .status("SUCCESS")
                    .executionTimeMs((int) executionTimeMs)
                    .build();

            toolCallRecordMapper.insert(record);
            log.debug("工具调用记录已保存：toolName={}, time={}ms", toolName, executionTimeMs);
        } catch (Exception e) {
            log.error("保存工具调用记录失败：toolName={}", toolName, e);
        }
    }

    /**
     * 记录工具调用（失败）
     *
     * @param toolName         工具名称
     * @param toolInput        工具输入参数
     * @param exception        异常信息
     * @param startTimeMs      开始时间（毫秒）
     */
    public static void recordFailure(String toolName, Object toolInput, Exception exception, long startTimeMs) {
        Long messageId = getCurrentAssistantMessageId();
        if (messageId == null) {
            log.debug("无消息ID上下文，跳过工具调用记录：toolName={}", toolName);
            return;
        }

        try {
            long executionTimeMs = System.currentTimeMillis() - startTimeMs;

            ToolCallRecord record = ToolCallRecord.builder()
                    .messageId(messageId)
                    .toolName(toolName)
                    .toolInput(toJson(toolInput))
                    .status("FAILURE")
                    .errorMessage(exception.getMessage())
                    .executionTimeMs((int) executionTimeMs)
                    .build();

            toolCallRecordMapper.insert(record);
            log.debug("工具调用失败记录已保存：toolName={}, error={}", toolName, exception.getMessage());
        } catch (Exception e) {
            log.error("保存工具调用记录失败：toolName={}", toolName, e);
        }
    }

    /**
     * 对象转 JSON 字符串
     */
    private static String toJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.warn("JSON 序列化失败", e);
            return String.valueOf(obj);
        }
    }
}
