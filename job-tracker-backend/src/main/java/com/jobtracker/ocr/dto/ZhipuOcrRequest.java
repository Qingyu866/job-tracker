package com.jobtracker.ocr.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.Base64;

/**
 * 智谱 OCR API 请求
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 2026-03-16
 */
@Data
@Builder
public class ZhipuOcrRequest {

    /**
     * 图片的 Base64 编码（需要添加 data URI 前缀）
     */
    @JsonProperty("image")
    private String imageBase64;

    /**
     * 是否启用表格识别
     */
    @JsonProperty("table")
    private Boolean enableTable;

    /**
     * 是否启用版面分析
     */
    @JsonProperty("layout")
    private Boolean enableLayout;

    /**
     * 是否启用公式识别
     */
    @JsonProperty("formula")
    private Boolean enableFormula;

    /**
     * 从字节数组创建请求
     */
    public static ZhipuOcrRequest fromBytes(byte[] imageBytes, OcrOptions options) {
        // 转换为 Base64，并添加 data URI 前缀
        String base64 = Base64.getEncoder().encodeToString(imageBytes);
        String dataUri = "data:image/jpeg;base64," + base64;

        return ZhipuOcrRequest.builder()
                .imageBase64(dataUri)
                .enableTable(options.isEnableTable())
                .enableLayout(options.isEnableLayout())
                .enableFormula(options.isEnableFormula())
                .build();
    }
}
