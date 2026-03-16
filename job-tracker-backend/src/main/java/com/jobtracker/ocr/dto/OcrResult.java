package com.jobtracker.ocr.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * OCR 识别结果
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 2026-03-16
 */
@Data
@Builder
public class OcrResult {

    /**
     * 识别状态
     */
    private OcrStatus status;

    /**
     * 识别的文本内容
     */
    private String text;

    /**
     * 置信度（0.0 - 1.0）
     */
    private Double confidence;

    /**
     * 处理耗时（毫秒）
     */
    private Long processingTimeMs;

    /**
     * OCR 提供商
     */
    private String provider;

    /**
     * 错误代码（失败时）
     */
    private String errorCode;

    /**
     * 错误信息（失败时）
     */
    private String errorMessage;

    /**
     * 识别时间
     */
    private LocalDateTime timestamp;

    /**
     * 是否使用了降级方案（本地模型）
     */
    private boolean isFallback;

    /**
     * 识别状态枚举
     */
    public enum OcrStatus {
        /**
         * 识别成功
         */
        SUCCESS,

        /**
         * 识别失败
         */
        FAILED,

        /**
         * 使用降级方案
         */
        FALLBACK,

        /**
         * 超时
         */
        TIMEOUT
    }

    /**
     * 创建成功结果
     */
    public static OcrResult success(String text, Double confidence, long processingTimeMs, String provider) {
        return OcrResult.builder()
                .status(OcrStatus.SUCCESS)
                .text(text)
                .confidence(confidence)
                .processingTimeMs(processingTimeMs)
                .provider(provider)
                .timestamp(LocalDateTime.now())
                .isFallback(false)
                .build();
    }

    /**
     * 创建降级结果
     */
    public static OcrResult fallback(String text, Double confidence, long processingTimeMs) {
        return OcrResult.builder()
                .status(OcrStatus.FALLBACK)
                .text(text)
                .confidence(confidence)
                .processingTimeMs(processingTimeMs)
                .provider("LOCAL_GEMMA")
                .timestamp(LocalDateTime.now())
                .isFallback(true)
                .build();
    }

    /**
     * 创建失败结果
     */
    public static OcrResult failed(String errorCode, String errorMessage) {
        return OcrResult.builder()
                .status(OcrStatus.FAILED)
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .timestamp(LocalDateTime.now())
                .isFallback(false)
                .build();
    }

    /**
     * 判断是否成功（包括降级）
     */
    public boolean isSuccess() {
        return status == OcrStatus.SUCCESS || status == OcrStatus.FALLBACK;
    }
}
