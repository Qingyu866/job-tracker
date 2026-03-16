package com.jobtracker.agent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jobtracker.dto.ImageAttachment;
import com.jobtracker.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.List;

/**
 * 多模态 JobAgent 实现类
 * <p>
 * 直接调用 LM Studio API 实现真正的视觉理解能力
 * </p>
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 2026-03-15
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MultimodalJobAgent implements JobAgent {

    private final FileStorageService fileStorageService;
    private final ObjectMapper objectMapper;

    @Value("${langchain4j.lm-studio.base-url:http://localhost:1234/v1}")
    private String baseUrl;

    @Value("${langchain4j.lm-studio.model-name:google/gemma-3-4b}")
    private String modelName;

    @Value("${langchain4j.lm-studio.api-key:lm-studio}")
    private String apiKey;

    private static final String SYSTEM_PROMPT = """
            你是 JobTracker 智能助手，专门帮助用户管理求职申请流程。

            ## 职责
            - 帮助用户创建和查询求职申请
            - 管理面试记录和反馈
            - 提供求职统计数据和趋势分析
            - **支持图片分析**：可以分析用户上传的简历截图、职位描述、公司 logo 等

            当用户发送图片时，请仔细分析图片内容，并结合用户的文字说明提供帮助。

            如果用户上传的是简历，请提取关键信息如：姓名、联系方式、教育背景、工作经历、技能等。
            如果用户上传的是职位描述，请提取关键信息如：职位名称、公司、要求、福利等。

            保持专业、友好的语气。
            """;

    @Override
    public String chat(String userMessage, String currentDate, String currentTime, String dayOfWeek) {
        // 纯文本消息，构建完整的提示词
        String fullPrompt = buildTimePrompt(currentDate, currentTime, dayOfWeek) + userMessage;
        return callLmStudio(fullPrompt, null);
    }

    @Override
    public String chatWithImages(String userMessage, List<ImageAttachment> images,
                                  String currentDate, String currentTime, String dayOfWeek) {
        // 构建带时间信息的提示词
        String timePrompt = buildTimePrompt(currentDate, currentTime, dayOfWeek);

        // 如果有文字消息，添加时间信息
        String content = (userMessage != null && !userMessage.isEmpty())
                ? timePrompt + userMessage
                : timePrompt + "请分析我上传的图片。";

        return callLmStudio(content, images);
    }

    /**
     * 构建时间提示词
     */
    private String buildTimePrompt(String currentDate, String currentTime, String dayOfWeek) {
        return String.format("""
                [当前时间信息]
                - 日期：%s
                - 时间：%s
                - 星期：%s

                请基于以上时间回答用户的提问。

                ---
                用户消息：
                """, currentDate, currentTime, dayOfWeek);
    }

    /**
     * 调用 LM Studio API（支持多模态）
     */
    private String callLmStudio(String userMessage, List<ImageAttachment> images) {
        try {
            // 构建请求体
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", modelName);
            requestBody.put("temperature", 0.7);

            // 构建消息数组
            ArrayNode messages = requestBody.putArray("messages");

            // 添加系统消息
            ObjectNode systemMessage = messages.addObject();
            systemMessage.put("role", "system");
            systemMessage.put("content", SYSTEM_PROMPT);

            // 添加用户消息（可能包含图片）
            ObjectNode userMsg = messages.addObject();
            userMsg.put("role", "user");

            if (images != null && !images.isEmpty()) {
                // 多模态消息
                ArrayNode content = userMsg.putArray("content");

                // 添加文字内容
                if (userMessage != null && !userMessage.isEmpty()) {
                    ObjectNode textContent = content.addObject();
                    textContent.put("type", "text");
                    textContent.put("text", userMessage);
                }

                // 添加图片内容
                for (ImageAttachment image : images) {
                    try {
                        // 直接使用 filePath 字段读取图片文件
                        byte[] imageBytes = fileStorageService.readFileAsBytes(image.getFilePath());
                        String base64 = Base64.getEncoder().encodeToString(imageBytes);

                        ObjectNode imageContent = content.addObject();
                        imageContent.put("type", "image_url");

                        ObjectNode imageUrl = imageContent.putObject("image_url");
                        imageUrl.put("url", "data:" + image.getMimeType() + ";base64," + base64);

                        log.debug("添加图片到多模态消息：id={}, type={}, size={}KB",
                                image.getId(), image.getMimeType(), imageBytes.length / 1024);

                    } catch (Exception e) {
                        log.error("处理图片失败：id={}", image.getId(), e);
                    }
                }
            } else {
                // 纯文本消息
                userMsg.put("content", userMessage);
            }

            // 发送 HTTP 请求
            RestTemplate restTemplate = new RestTemplate();
            String url = baseUrl + "/chat/completions";

            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(
                    requestBody.toString(), headers);

            org.springframework.http.ResponseEntity<String> response = restTemplate.postForEntity(
                    url, entity, String.class);

            // 解析响应
            JsonNode responseJson = objectMapper.readTree(response.getBody());
            String aiResponse = responseJson.path("choices")
                    .path(0)
                    .path("message")
                    .path("content")
                    .asText();

            log.info("LM Studio 响应成功：responseLength={}", aiResponse != null ? aiResponse.length() : 0);

            return aiResponse != null ? aiResponse : "抱歉，没有收到响应。";

        } catch (Exception e) {
            log.error("调用 LM Studio 失败", e);
            return "抱歉，处理请求时出现错误：" + e.getMessage();
        }
    }
}
