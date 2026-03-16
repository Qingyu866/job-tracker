package com.jobtracker.ocr.service;

import com.jobtracker.ocr.ZhipuOcrClient;
import com.jobtracker.ocr.dto.OcrOptions;
import com.jobtracker.ocr.dto.OcrResult;
import com.jobtracker.ocr.dto.OcrResult.OcrStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * OCR 核心识别服务
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 2026-03-16
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OcrService {

    private final ZhipuOcrClient zhipuOcrClient;

    @Value("${ocr.enabled:true}")
    private boolean ocrEnabled;

    @Value("${ocr.default-provider:ZHIPU}")
    private String defaultProvider;

    @Value("${ocr.fallback.enable-local-model:true}")
    private boolean enableLocalFallback;

    @Value("${ocr.limits.max-image-size:10485760}")
    private int maxImageSize;

    @Value("${ocr.limits.supported-formats:jpg,jpeg,png,pdf}")
    private String supportedFormats;

    /**
     * 识别图片
     *
     * @param imageData 图片字节数据
     * @param options   识别选项
     * @return 识别结果
     */
    public OcrResult recognize(byte[] imageData, OcrOptions options) {
        // 1. 验证输入
        if (!validateInput(imageData)) {
            return OcrResult.failed("INVALID_INPUT", "图片数据无效");
        }

        // 2. 检查 OCR 是否启用
        if (!ocrEnabled) {
            log.debug("OCR 功能已禁用，返回失败结果");
            return OcrResult.failed("OCR_DISABLED", "OCR 功能未启用");
        }

        // 3. 尝试智谱 OCR
        if ("ZHIPU".equals(defaultProvider)) {
            if (zhipuOcrClient.isConfigured()) {
                OcrResult result = tryZhipuOcr(imageData, options);
                if (result.isSuccess()) {
                    return result;
                }
                // 智谱 OCR 失败，尝试降级
                if (enableLocalFallback) {
                    log.info("智谱 OCR 失败，尝试使用本地模型降级");
                    return fallbackToLocal(result.getErrorMessage());
                }
                return result;
            } else {
                log.warn("智谱 OCR 未配置，使用本地模型");
                return fallbackToLocal("智谱 OCR 未配置");
            }
        }

        // 4. 直接使用本地模型
        return fallbackToLocal("配置为使用本地模型");
    }

    /**
     * 尝试使用智谱 OCR
     */
    private OcrResult tryZhipuOcr(byte[] imageData, OcrOptions options) {
        try {
            return zhipuOcrClient.recognize(imageData, options);
        } catch (Exception e) {
            log.error("智谱 OCR 调用异常: {}", e.getMessage(), e);
            return OcrResult.failed("ZHIPU_ERROR", "智谱 OCR 调用失败: " + e.getMessage());
        }
    }

    /**
     * 降级到本地模型
     *
     * 注意：当前实现返回占位结果
     * 实际使用时需要集成 MultimodalJobAgent
     */
    private OcrResult fallbackToLocal(String reason) {
        log.info("使用本地模型降级，原因: {}", reason);

        // TODO: 实际集成时需要调用 MultimodalJobAgent
        // 当前返回占位结果
        return OcrResult.failed("FALLBACK_NOT_IMPLEMENTED",
                "本地模型降级暂未实现，原因: " + reason);
    }

    /**
     * 验证输入数据
     */
    private boolean validateInput(byte[] imageData) {
        if (imageData == null || imageData.length == 0) {
            log.warn("图片数据为空");
            return false;
        }

        if (imageData.length > maxImageSize) {
            log.warn("图片超过最大大小限制: {} > {}", imageData.length, maxImageSize);
            return false;
        }

        // 简单的格式检查（通过文件头判断）
        if (!isValidImageFormat(imageData)) {
            log.warn("不支持的图片格式");
            return false;
        }

        return true;
    }

    /**
     * 检查图片格式是否有效
     */
    private boolean isValidImageFormat(byte[] imageData) {
        if (imageData.length < 4) {
            return false;
        }

        // JPEG: FF D8 FF
        if (imageData[0] == (byte) 0xFF
                && imageData[1] == (byte) 0xD8
                && imageData[2] == (byte) 0xFF) {
            return true;
        }

        // PNG: 89 50 4E 47
        if (imageData[0] == (byte) 0x89
                && imageData[1] == (byte) 0x50
                && imageData[2] == (byte) 0x4E
                && imageData[3] == (byte) 0x47) {
            return true;
        }

        return false;
    }

    /**
     * 检查是否支持指定格式
     */
    public boolean isFormatSupported(String format) {
        List<String> formats = Arrays.asList(supportedFormats.split(","));
        return formats.contains(format.toLowerCase());
    }

    /**
     * 获取支持的格式列表
     */
    public List<String> getSupportedFormats() {
        return Arrays.asList(supportedFormats.split(","));
    }
}
