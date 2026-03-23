package com.jobtracker.ocr;

import ai.z.openapi.ZhipuAiClient;
import ai.z.openapi.service.fileparsing.FileParsingDownloadReq;
import ai.z.openapi.service.fileparsing.FileParsingDownloadResponse;
import ai.z.openapi.service.fileparsing.FileParsingResponse;
import ai.z.openapi.service.fileparsing.FileParsingUploadReq;
import com.jobtracker.ocr.dto.OcrOptions;
import com.jobtracker.ocr.dto.OcrResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.TimeUnit;

/**
 * 智谱文档解析客户端（使用官方SDK）
 * <p>
 * 使用智谱AI文档解析资源包（FileParsing API）
 * 支持的文件格式：PDF, DOCX, 图片等
 * </p>
 * <p>
 * API说明：
 * 1. createParseTask() - 上传文件并创建解析任务（返回taskId）
 * 2. getParseResult() - 轮询获取解析结果（异步）
 * </p>
 *
 * @author Job Tracker Team
 * @version 3.0.0
 * @since 2026-03-19
 */
@Slf4j
@Component
public class ZhipuOcrClient {

    @Value("${ocr.zhipu.api-key}")
    private String apiKey;

    @Value("${ocr.zhipu.tool-type:prime-sync}")
    private String toolType;  // 解析工具类型：prime-sync(同步解析，推荐)

    @Value("${ocr.zhipu.timeout:300000}")
    private int timeout;  // 总超时时间（5分钟，包含轮询等待）

    @Value("${ocr.zhipu.poll-interval:3000}")
    private int pollInterval;  // 轮询间隔（3秒）

    @Value("${ocr.zhipu.max-retries:100}")
    private int maxRetries;  // 最大轮询次数（100次 * 3秒 = 5分钟）

    private ZhipuAiClient client;

    /**
     * 初始化客户端（懒加载）
     */
    private ZhipuAiClient getClient() {
        if (client == null) {
            synchronized (this) {
                if (client == null) {
                    client = ZhipuAiClient.builder()
                            .ofZHIPU()
                            .apiKey(apiKey)
                            .build();
                    log.info("智谱AI客户端初始化成功，解析工具: {}", toolType);
                }
            }
        }
        return client;
    }

    /**
     * 调用文档解析（通过文件路径）
     * <p>
     * 使用官方同步解析API（syncParse），无需轮询
     * </p>
     *
     * @param filePath 文件路径（支持PDF、DOCX、图片等）
     * @param options  识别选项（保留兼容性）
     * @return 识别结果
     */
    public OcrResult recognize(String filePath, OcrOptions options) {
        long startTime = System.currentTimeMillis();

        try {
            log.info("开始文档解析（同步模式），文件路径: {}, 解析工具: {}", filePath, toolType);

            // 1. 确定文件类型
            String fileType = getFileType(filePath);

            // 2. 构建同步解析请求
            FileParsingUploadReq uploadReq = FileParsingUploadReq.builder()
                    .filePath(filePath)
                    .fileType(fileType)
                    .toolType(toolType)
                    .build();

            // 3. 调用同步解析API（官方推荐方式）
            FileParsingDownloadResponse response = getClient().fileParsing().syncParse(uploadReq);

            long processingTime = System.currentTimeMillis() - startTime;

            // 4. 处理响应
            if (response != null && response.isSuccess()) {
                String status = response.getData().getStatus();

                if ("succeeded".equalsIgnoreCase(status)) {
                    String text = response.getData().getContent();

                    log.info("文档解析成功，文本长度: {}, 耗时: {} ms", text.length(), processingTime);
                    log.info("========== 解析内容开始 ==========");
                    log.info("{}", text);
                    log.info("========== 解析内容结束 ==========");

                    return OcrResult.success(text, 0.95, processingTime, "ZHIPU");
                } else {
                    log.error("文档解析失败，状态: {}, 消息: {}", status, response.getData().getMessage());
                    return OcrResult.failed("PARSE_FAILED", "文档解析失败: " + response.getData().getMessage());
                }
            } else {
                String errorMsg = response != null ? response.getMsg() : "响应为空";
                log.error("同步解析调用失败: {}", errorMsg);
                return OcrResult.failed("SDK_ERROR", "同步解析调用失败: " + errorMsg);
            }

        } catch (Exception e) {
            long processingTime = System.currentTimeMillis() - startTime;
            log.error("文档解析异常: {}", e.getMessage(), e);
            return OcrResult.failed("SDK_ERROR", "文档解析异常: " + e.getMessage());
        }
    }

    /**
     * 调用文档解析（通过MultipartFile）
     * <p>
     * Spring MVC上传的文件需要先保存为临时文件
     * </p>
     *
     * @param file     上传的文件
     * @param options  识别选项（保留兼容性）
     * @return 识别结果
     */
    public OcrResult recognize(MultipartFile file, OcrOptions options) {
        try {
            // 1. 保存为临时文件
            Path tempFile = Files.createTempFile("ocr_", "_" + file.getOriginalFilename());
            Files.copy(file.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);

            log.info("临时文件已创建: {}", tempFile);

            // 2. 调用解析
            OcrResult result = recognize(tempFile.toString(), options);

            // 3. 清理临时文件
            try {
                Files.deleteIfExists(tempFile);
                log.debug("临时文件已删除: {}", tempFile);
            } catch (IOException e) {
                log.warn("删除临时文件失败: {}", e.getMessage());
            }

            return result;

        } catch (Exception e) {
            log.error("处理上传文件失败: {}", e.getMessage(), e);
            return OcrResult.failed("FILE_ERROR", "处理上传文件失败: " + e.getMessage());
        }
    }

    /**
     * 调用文档解析（通过字节数组）
     * <p>
     * 兼容旧接口，将字节数组保存为临时文件
     * </p>
     *
     * @param imageData 图片字节数组
     * @param options   识别选项
     * @return 识别结果
     */
    public OcrResult recognize(byte[] imageData, OcrOptions options) {
        try {
            // 1. 保存为临时文件
            Path tempFile = Files.createTempFile("ocr_", ".png");
            Files.write(tempFile, imageData);

            log.info("临时文件已创建: {}", tempFile);

            // 2. 调用解析
            OcrResult result = recognize(tempFile.toString(), options);

            // 3. 清理临时文件
            try {
                Files.deleteIfExists(tempFile);
                log.debug("临时文件已删除: {}", tempFile);
            } catch (IOException e) {
                log.warn("删除临时文件失败: {}", e.getMessage());
            }

            return result;

        } catch (Exception e) {
            log.error("处理字节数组失败: {}", e.getMessage(), e);
            return OcrResult.failed("BYTE_ERROR", "处理字节数组失败: " + e.getMessage());
        }
    }

    /**
     * 根据文件扩展名确定文件类型
     *
     * @param filePath 文件路径
     * @return 文件类型（pdf, docx, png, jpg等）
     */
    private String getFileType(String filePath) {
        String fileName = new File(filePath).getName();
        int lastDotIndex = fileName.lastIndexOf('.');

        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            String extension = fileName.substring(lastDotIndex + 1).toLowerCase();
            return extension;
        }

        // 默认为PDF
        return "pdf";
    }

    /**
     * 健康检查
     */
    public boolean healthCheck() {
        try {
            return getClient() != null && isConfigured();
        } catch (Exception e) {
            log.warn("智谱文档解析健康检查失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 检查配置是否完整
     */
    public boolean isConfigured() {
        return apiKey != null
                && !apiKey.isBlank()
                && !apiKey.equals("${ocr.zhipu.api-key}");
    }
}
