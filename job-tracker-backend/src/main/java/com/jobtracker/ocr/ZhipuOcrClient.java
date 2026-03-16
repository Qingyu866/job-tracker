package com.jobtracker.ocr;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobtracker.ocr.dto.OcrOptions;
import com.jobtracker.ocr.dto.OcrResult;
import com.jobtracker.ocr.dto.ZhipuOcrRequest;
import com.jobtracker.ocr.dto.ZhipuOcrResponse;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 智谱 OCR 云服务客户端
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 2026-03-16
 */
@Slf4j
@Component
public class ZhipuOcrClient {

    private static final MediaType JSON_TYPE = MediaType.parse("application/json; charset=utf-8");

    @Value("${ocr.zhipu.api-url:https://open.bigmodel.cn/api/paas/v4/ocr}")
    private String apiUrl;

    @Value("${ocr.zhipu.api-key}")
    private String apiKey;

    @Value("${ocr.zhipu.timeout:30000}")
    private int timeout;

    private final ObjectMapper objectMapper;
    private final OkHttpClient httpClient;

    public ZhipuOcrClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(timeout, TimeUnit.MILLISECONDS)
                .readTimeout(timeout, TimeUnit.MILLISECONDS)
                .writeTimeout(timeout, TimeUnit.MILLISECONDS)
                .build();
    }

    /**
     * 调用 OCR 识别图片
     *
     * @param imageData 图片字节数据
     * @param options    识别选项
     * @return 识别结果
     */
    public OcrResult recognize(byte[] imageData, OcrOptions options) {
        long startTime = System.currentTimeMillis();

        try {
            // 1. 构建请求
            ZhipuOcrRequest request = ZhipuOcrRequest.fromBytes(imageData, options);
            String requestBody = objectMapper.writeValueAsString(request);

            log.debug("发送 OCR 请求到智谱 API，图片大小: {} bytes", imageData.length);

            // 2. 构建 HTTP 请求
            Request httpRequest = new Request.Builder()
                    .url(apiUrl)
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(requestBody, JSON_TYPE))
                    .build();

            // 3. 发送请求
            try (Response response = httpClient.newCall(httpRequest).execute()) {
                long processingTime = System.currentTimeMillis() - startTime;

                if (!response.isSuccessful()) {
                    log.error("OCR API 调用失败，HTTP 状态码: {}", response.code());
                    return OcrResult.failed(
                            "HTTP_ERROR",
                            "HTTP " + response.code() + ": " + response.message()
                    );
                }

                // 4. 解析响应
                String responseBody = response.body() != null ? response.body().string() : "";
                ZhipuOcrResponse ocrResponse = objectMapper.readValue(
                        responseBody,
                        ZhipuOcrResponse.class
                );

                if (!ocrResponse.isSuccess()) {
                    log.error("OCR API 返回错误: {}", ocrResponse.getErrorMessage());
                    return OcrResult.failed(
                            "API_ERROR",
                            ocrResponse.getErrorMessage()
                    );
                }

                // 5. 提取结果
                String text = ocrResponse.getData() != null
                        ? ocrResponse.getData().getText()
                        : "";
                Double confidence = ocrResponse.getData() != null
                        ? ocrResponse.getData().getConfidence()
                        : 0.0;

                log.info("OCR 识别成功，文本长度: {}, 置信度: {}, 耗时: {} ms",
                        text.length(), confidence, processingTime);

                return OcrResult.success(text, confidence, processingTime, "ZHIPU");

            } catch (IOException e) {
                long processingTime = System.currentTimeMillis() - startTime;
                log.error("OCR API 调用异常: {}", e.getMessage(), e);
                return OcrResult.failed("IO_ERROR", "网络异常: " + e.getMessage());
            }

        } catch (Exception e) {
            log.error("OCR 识别失败: {}", e.getMessage(), e);
            return OcrResult.failed("UNKNOWN_ERROR", "未知错误: " + e.getMessage());
        }
    }

    /**
     * 健康检查
     *
     * @return API 是否可用
     */
    public boolean healthCheck() {
        try {
            // 发送一个简单的请求测试连接
            Request request = new Request.Builder()
                    .url(apiUrl.replace("/ocr", "/ping"))  // 假设有 ping 端点
                    .head()
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                return response.isSuccessful() || response.code() == 404;  // 404 也说明服务在线
            }
        } catch (Exception e) {
            log.warn("智谱 OCR 健康检查失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 检查配置是否完整
     */
    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank() && !apiKey.equals("${ocr.zhipu.api-key}");
    }
}
