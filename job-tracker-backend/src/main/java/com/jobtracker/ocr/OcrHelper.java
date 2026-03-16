package com.jobtracker.ocr;

import com.jobtracker.ocr.dto.OcrOptions;
import com.jobtracker.ocr.dto.OcrResult;
import com.jobtracker.ocr.service.OcrService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Base64;

/**
 * OCR 辅助工具类
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 2026-03-16
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OcrHelper {

    private final OcrService ocrService;

    /**
     * 检查是否应该使用 OCR
     *
     * @param imageData 图片数据
     * @return 是否应该使用 OCR
     */
    public boolean shouldUseOcr(byte[] imageData) {
        if (imageData == null || imageData.length == 0) {
            return false;
        }

        // 检查图片大小（超过 100KB 建议使用 OCR）
        if (imageData.length > 100 * 1024) {
            return true;
        }

        return true;
    }

    /**
     * 从 Base64 字符串识别图片
     *
     * @param base64Data Base64 编码的图片（可能包含 data URI 前缀）
     * @param options    识别选项
     * @return 识别结果
     */
    public OcrResult recognizeFromBase64(String base64Data, OcrOptions options) {
        try {
            // 移除 data URI 前缀（如果有）
            String base64String = base64Data;
            if (base64Data.contains(",")) {
                base64String = base64Data.substring(base64Data.indexOf(",") + 1);
            }

            byte[] imageData = Base64.getDecoder().decode(base64String);
            return ocrService.recognize(imageData, options);

        } catch (IllegalArgumentException e) {
            log.error("Base64 解码失败: {}", e.getMessage());
            return OcrResult.failed("INVALID_BASE64", "Base64 编码无效");
        }
    }

    /**
     * 格式化 OCR 结果供 AI 使用
     *
     * @param result OCR 识别结果
     * @return 格式化的文本
     */
    public String formatOcrResult(OcrResult result) {
        if (!result.isSuccess()) {
            return String.format("[OCR 识别失败: %s]", result.getErrorMessage());
        }

        StringBuilder sb = new StringBuilder();
        sb.append("图片识别结果：\n\n");

        // 添加文本内容
        sb.append(result.getText());

        // 添加元数据
        sb.append("\n\n---\n");
        sb.append(String.format("识别方式: %s\n", result.isFallback() ? "本地模型" : "智谱 OCR"));
        if (result.getConfidence() != null) {
            sb.append(String.format("置信度: %.1f%%\n", result.getConfidence() * 100));
        }
        if (result.getProcessingTimeMs() != null) {
            sb.append(String.format("耗时: %d ms\n", result.getProcessingTimeMs()));
        }

        return sb.toString();
    }

    /**
     * 从简历文本中提取结构化信息（占位实现）
     *
     * TODO: 实际实现需要调用 AI 解析
     */
    public ResumeInfo extractResumeInfo(String ocrText) {
        // 当前返回基础信息
        // 实际实现应该调用 AI Agent 解析
        return ResumeInfo.builder()
                .rawText(ocrText)
                .workYears(extractWorkYears(ocrText))
                .currentPosition(extractCurrentPosition(ocrText))
                .summary(ocrText.substring(0, Math.min(200, ocrText.length())))
                .build();
    }

    /**
     * 从 JD 文本中提取技能标签（占位实现）
     *
     * TODO: 实际实现需要调用 AI 解析
     */
    public java.util.List<String> extractSkillsFromJD(String ocrText) {
        // 当前返回空列表
        // 实际实现应该调用 AI Agent 解析
        return java.util.Collections.emptyList();
    }

    /**
     * 简单提取工作年限（占位实现）
     */
    private Integer extractWorkYears(String text) {
        // 简单的正则匹配
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d+)\\s*年");
        java.util.regex.Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * 简单提取当前职位（占位实现）
     */
    private String extractCurrentPosition(String text) {
        // 查找包含"职位"、"岗位"等关键词的行
        String[] lines = text.split("\n");
        for (String line : lines) {
            if (line.contains("职位") || line.contains("岗位")) {
                return line.trim();
            }
        }
        return null;
    }

    /**
     * 简历信息 DTO
     */
    @lombok.Data
    @lombok.Builder
    public static class ResumeInfo {
        private String rawText;
        private Integer workYears;
        private String currentPosition;
        private String summary;
    }
}
