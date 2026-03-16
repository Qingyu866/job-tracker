package com.jobtracker.ocr.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 智谱 OCR API 响应
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 2026-03-16
 */
@Data
public class ZhipuOcrResponse {

    /**
     * 响应代码
     */
    @JsonProperty("code")
    private Integer code;

    /**
     * 响应消息
     */
    @JsonProperty("msg")
    private String msg;

    /**
     * 识别结果数据
     */
    @JsonProperty("data")
    private OcrData data;

    /**
     * OCR 数据
     */
    @Data
    public static class OcrData {
        /**
         * 识别的文本内容
         */
        @JsonProperty("text")
        private String text;

        /**
         * 置信度
         */
        @JsonProperty("confidence")
        private Double confidence;

        /**
         * 识别的块列表（用于版面分析）
         */
        @JsonProperty("blocks")
        private List<TextBlock> blocks;

        /**
         * 表格数据（如果启用表格识别）
         */
        @JsonProperty("tables")
        private List<TableData> tables;
    }

    /**
     * 文本块
     */
    @Data
    public static class TextBlock {
        /**
         * 块类型
         */
        @JsonProperty("type")
        private String type;

        /**
         * 文本内容
         */
        @JsonProperty("text")
        private String text;

        /**
         * 置信度
         */
        @JsonProperty("confidence")
        private Double confidence;

        /**
         * 位置信息
         */
        @JsonProperty("box")
        private List<Integer> box;
    }

    /**
     * 表格数据
     */
    @Data
    public static class TableData {
        /**
         * 表格文本（HTML 格式）
         */
        @JsonProperty("html")
        private String html;
    }

    /**
     * 判断是否成功
     */
    public boolean isSuccess() {
        return code != null && code == 200;
    }

    /**
     * 获取错误消息
     */
    public String getErrorMessage() {
        if (isSuccess()) {
            return null;
        }
        return msg != null ? msg : "Unknown error (code: " + code + ")";
    }
}
