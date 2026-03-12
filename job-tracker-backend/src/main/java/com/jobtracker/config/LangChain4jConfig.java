package com.jobtracker.config;

import com.jobtracker.agent.JobAgent;
import com.jobtracker.agent.tools.ApplicationTools;
import com.jobtracker.agent.tools.CompanyTools;
import com.jobtracker.agent.tools.InterviewTools;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.function.Supplier;

import static dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O_MINI;

/**
 * LangChain4j 配置类
 * <p>
 * 配置 AI Agent 相关的 Bean，包括：
 * - StreamingChatModel: 流式聊天模型（连接 LM Studio）
 * - ChatMemoryProvider: 聊天记忆提供器
 * - JobAgent: AI 服务实例
 * </p>
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Configuration
public class LangChain4jConfig {

    /**
     * LM Studio API 基础 URL
     */
    @Value("${langchain4j.lm-studio.base-url:http://localhost:1234/v1}")
    private String baseUrl;

    /**
     * LM Studio API 密钥（LM Studio 不需要，但 OpenAI 客户端要求）
     */
    @Value("${langchain4j.lm-studio.api-key:lm-studio}")
    private String apiKey;

    /**
     * 模型名称
     */
    @Value("${langchain4j.lm-studio.model-name:gemma-3-4b-it}")
    private String modelName;

    /**
     * 温度参数（控制响应的随机性，0.0-2.0）
     */
    @Value("${langchain4j.lm-studio.temperature:0.7}")
    private Double temperature;

    /**
     * 超时时间（秒）
     */
    @Value("${langchain4j.lm-studio.timeout:60}")
    private Integer timeout;

    /**
     * 聊天记忆窗口大小（保留最近的消息数量）
     */
    @Value("${langchain4j.chat-memory.window-size:20}")
    private Integer chatMemoryWindowSize;

    /**
     * 配置流式聊天模型
     * <p>
     * 使用 OpenAI 兼容的接口连接 LM Studio
     * 支持流式输出，提供更好的用户体验
     * </p>
     *
     * @return StreamingChatModel 实例
     */
    @Bean
    public StreamingChatModel streamingChatModel() {
        return OpenAiStreamingChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .temperature(temperature)
                .timeout(Duration.ofSeconds(timeout))
                .build();
    }

    /**
     * 配置非流式聊天模型（用于某些不需要流式输出的场景）
     *
     * @return OpenAiChatModel 实例
     */
    @Bean
    public OpenAiChatModel chatModel() {
        return OpenAiChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .temperature(temperature)
                .timeout(Duration.ofSeconds(timeout))
                .build();
    }

    /**
     * 配置聊天记忆提供器
     * <p>
     * 为每个会话维护独立的聊天记忆
     * 使用滑动窗口策略，保留最近的 N 条消息
     * </p>
     *
     * @return ChatMemoryProvider 实例
     */
    @Bean
    public ChatMemoryProvider chatMemoryProvider() {
        return memoryId -> MessageWindowChatMemory.builder()
                .maxMessages(chatMemoryWindowSize)
                .id(memoryId)
                .build();
    }

    /**
     * 配置 Job Agent
     * <p>
     * 集成聊天模型、聊天记忆和工具方法
     * 创建完整的 AI 服务实例
     * </p>
     *
     * @param streamingChatModel 流式聊天模型
     * @param chatMemoryProvider 聊天记忆提供器
     * @param applicationTools   申请工具方法
     * @param interviewTools     面试工具方法
     * @param companyTools       公司工具方法
     * @return JobAgent 实例
     */
    @Bean
    public JobAgent jobAgent(
            StreamingChatModel streamingChatModel,
            ChatMemoryProvider chatMemoryProvider,
            ApplicationTools applicationTools,
            InterviewTools interviewTools,
            CompanyTools companyTools
    ) {
        return AiServices.builder(JobAgent.class)
                .streamingChatModel(streamingChatModel)
                .chatMemoryProvider(chatMemoryProvider)
                .tools(applicationTools, interviewTools, companyTools)
                .build();
    }
}
