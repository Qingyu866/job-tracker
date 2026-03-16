package com.jobtracker.config;

import com.jobtracker.agent.JobAgent;
import com.jobtracker.agent.memory.SafeTurnBasedChatMemoryProvider;
import com.jobtracker.agent.tools.ApplicationTools;
import com.jobtracker.agent.tools.CompanyTools;
import com.jobtracker.agent.tools.InterviewTools;
import com.jobtracker.service.ChatHistoryService;
import dev.langchain4j.http.client.jdk.JdkHttpClient;
import dev.langchain4j.http.client.jdk.JdkHttpClientBuilder;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.service.AiServices;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.http.HttpClient;
import java.time.Duration;

import static dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O_MINI;

/**
 * LangChain4j 配置类
 * <p>
 * 配置 AI Agent 相关的 Bean，包括：
 * - StreamingChatModel: 流式聊天模型（连接 LM Studio）
 * - ChatMemoryProvider: 聊天记忆提供器（支持从数据库加载历史）
 * - JobAgent: AI 服务实例
 * </p>
 *
 * @author Job Tracker Team
 * @version 2.0.0
 * @since 1.0.0
 */
@Configuration
@RequiredArgsConstructor
public class LangChain4jConfig {

    private final ChatHistoryService chatHistoryService;

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
    @Value("${langchain4j.lm-studio.model-name:google/gemma-3-4b}")
    private String modelName;

    /**
     * 温度参数（控制响应的随机性，0.0-2.0）
     */
    @Value("${langchain4j.lm-studio.temperature:0.7}")
    private Double temperature;

    /**
     * 超时时间（秒）
     */
    @Value("${langchain4j.lm-studio.timeout:120}")
    private Integer timeout;

    /**
     * 聊天记忆窗口大小（保留最近的消息数量）
     */
    @Value("${langchain4j.chat-memory.window-size:20}")
    private Integer chatMemoryWindowSize;

    /**
     * 从数据库加载的历史消息数量
     */
    @Value("${langchain4j.chat-memory.history-load-limit:10}")
    private Integer historyLoadLimit;


    /**
     * 配置流式聊天模型
     * <p>
     * 使用 OpenAI 兼容的接口连接 LM Studio
     * 支持流式输出，提供更好的用户体验
     * 强制使用 HTTP/1.1 协议以兼容 LM Studio
     * </p>
     *
     * @return StreamingChatModel 实例
     */
    @Bean
    public StreamingChatModel streamingChatModel() {
        // 创建使用 HTTP/1.1 的 JDK HTTP 客户端
        HttpClient.Builder httpClientBuilder = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(timeout));

        JdkHttpClientBuilder jdkHttpClientBuilder = JdkHttpClient.builder()
                .httpClientBuilder(httpClientBuilder);

        return OpenAiStreamingChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .temperature(temperature)
                .timeout(Duration.ofSeconds(timeout))
                .httpClientBuilder(jdkHttpClientBuilder)
                .build();
    }

    /**
     * 配置非流式聊天模型（用于某些不需要流式输出的场景）
     *
     * @return OpenAiChatModel 实例
     */
    @Bean
    public OpenAiChatModel chatModel() {
        // 创建使用 HTTP/1.1 的 JDK HTTP 客户端
        HttpClient.Builder httpClientBuilder = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(timeout));

        JdkHttpClientBuilder jdkHttpClientBuilder = JdkHttpClient.builder()
                .httpClientBuilder(httpClientBuilder);

        return OpenAiChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .temperature(temperature)
                .timeout(Duration.ofSeconds(timeout))
                .httpClientBuilder(jdkHttpClientBuilder)
                .logResponses(true)
                .logRequests(true)
                .build();
    }

    /**
     * 配置聊天记忆提供器
     * <p>
     * 使用项目自定义的 SafeTurnBasedChatMemoryProvider：
     * - 为每个会话维护独立的聊天记忆
     * - 从数据库加载历史消息（服务重启后恢复）
     * - 消息序列验证和修复（避免 "Conversation roles must alternate" 错误）
     * - 基于轮次清理消息，不会打断对话
     * </p>
     *
     * @return SafeTurnBasedChatMemoryProvider 实例
     */
    @Bean
    public SafeTurnBasedChatMemoryProvider chatMemoryProvider() {
        return new SafeTurnBasedChatMemoryProvider(
                chatMemoryWindowSize,
                chatHistoryService,
                historyLoadLimit);
    }

    /**
     * 配置 Job Agent
     * <p>
     * 集成聊天模型、聊天记忆和工具方法
     * 创建完整的 AI 服务实例
     * </p>
     * <p>
     * <strong>注意：</strong>此单例 Bean 已弃用，改用 {@link com.jobtracker.agent.JobAgentFactory}
     * 实现真正的会话隔离。保留此 Bean 是为了向后兼容。
     * </p>
     *
     * @param chatModel         非流式聊天模型
     * @param streamingChatModel 流式聊天模型
     * @param chatMemoryProvider 聊天记忆提供者
     * @param applicationTools   申请工具方法
     * @param interviewTools     面试工具方法
     * @param companyTools       公司工具方法
     * @return JobAgent 实例
     */
    @Bean
    @Deprecated
    public JobAgent jobAgent(
            OpenAiChatModel chatModel,
            StreamingChatModel streamingChatModel,
            SafeTurnBasedChatMemoryProvider chatMemoryProvider,
            ApplicationTools applicationTools,
            InterviewTools interviewTools,
            CompanyTools companyTools
    ) {
        return AiServices.builder(JobAgent.class)
                .streamingChatModel(streamingChatModel)
                .chatModel(chatModel)
                .chatMemoryProvider(chatMemoryProvider)
                .tools(applicationTools, interviewTools, companyTools)
                .build();
    }
}
