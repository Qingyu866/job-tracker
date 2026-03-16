package com.jobtracker.ocr.dto;

import lombok.Builder;
import lombok.Data;

/**
 * OCR 识别选项
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 2026-03-16
 */
@Data
@Builder
public class OcrOptions {

    /**
     * 是否启用表格识别
     */
    private boolean enableTable;

    /**
     * 是否启用版面分析
     */
    private boolean enableLayout;

    /**
     * 是否启用公式识别
     */
    private boolean enableFormula;

    /**
     * 图片类型（用于优化识别）
     */
    private ImageType imageType;

    /**
     * 图片类型枚举
     */
    public enum ImageType {
        /**
         * 通用图片
         */
        GENERAL,

        /**
         * 简历图片
         */
        RESUME,

        /**
         * JD 图片
         */
        JD,

        /**
         * 表格图片
         */
        TABLE
    }

    /**
     * 默认选项
     */
    public static OcrOptions defaultOptions() {
        return OcrOptions.builder()
                .enableTable(false)
                .enableLayout(false)
                .enableFormula(false)
                .imageType(ImageType.GENERAL)
                .build();
    }

    /**
     * 简历识别选项
     */
    public static OcrOptions forResume() {
        return OcrOptions.builder()
                .enableTable(true)
                .enableLayout(true)
                .enableFormula(false)
                .imageType(ImageType.RESUME)
                .build();
    }

    /**
     * JD 识别选项
     */
    public static OcrOptions forJD() {
        return OcrOptions.builder()
                .enableTable(false)
                .enableLayout(true)
                .enableFormula(false)
                .imageType(ImageType.JD)
                .build();
    }
}
