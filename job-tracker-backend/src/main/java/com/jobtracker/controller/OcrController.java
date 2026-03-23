package com.jobtracker.controller;

import com.jobtracker.context.UserContext;
import com.jobtracker.common.result.Result;
import com.jobtracker.entity.OcrCallRecord;
import com.jobtracker.ocr.OcrHelper;
import com.jobtracker.ocr.OcrHelper.ResumeInfo;
import com.jobtracker.ocr.dto.OcrOptions;
import com.jobtracker.ocr.dto.OcrResult;
import com.jobtracker.ocr.service.OcrRecordService;
import com.jobtracker.ocr.service.OcrService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OCR API 控制器
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 2026-03-16
 */
@Slf4j
@RestController
@RequestMapping("/ocr")
@RequiredArgsConstructor
public class OcrController {

    private final OcrService ocrService;
    private final OcrHelper ocrHelper;
    private final OcrRecordService ocrRecordService;

    /**
     * 直接 OCR 识别
     * POST /api/ocr/recognize
     *
     * @param request 请求体
     * @return 识别结果
     */
    @PostMapping("/recognize")
    public Result<Map<String, Object>> recognize(@RequestBody OcrRecognizeRequest request) {
        try {
            // 解码 Base64
            byte[] imageData = Base64.getDecoder().decode(request.getImageData());

            // 识别
            OcrOptions options = request.getTableOnly() != null && request.getTableOnly()
                    ? OcrOptions.builder().enableTable(true).build()
                    : OcrOptions.defaultOptions();
            OcrResult result = ocrService.recognize(imageData, options);

            Map<String, Object> response = new HashMap<>();
            response.put("success", result.isSuccess());
            response.put("text", result.getText());
            response.put("confidence", result.getConfidence());
            response.put("provider", result.getProvider());
            response.put("processingTimeMs", result.getProcessingTimeMs());
            response.put("isFallback", result.isFallback());

            if (!result.isSuccess()) {
                response.put("error", result.getErrorMessage());
            }

            return Result.success(response);

        } catch (IllegalArgumentException e) {
            return Result.error("Base64 编码无效");
        } catch (Exception e) {
            log.error("OCR 识别失败", e);
            return Result.error("识别失败: " + e.getMessage());
        }
    }

    /**
     * 上传并识别简历
     * POST /api/ocr/resume
     *
     * @param file 简历文件（支持PDF、DOCX、图片等）
     * @return 解析后的简历信息
     */
    @PostMapping("/resume")
    public Result<Map<String, Object>> recognizeResume(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return Result.error("文件不能为空");
            }

            // 检查文件大小
            if (file.getSize() > 10 * 1024 * 1024) {
                return Result.error("文件大小不能超过 10MB");
            }

            log.info("开始识别简历，文件名: {}, 文件大小: {} bytes", file.getOriginalFilename(), file.getSize());

            // 直接使用 MultipartFile 识别（支持PDF、DOCX、图片等）
            OcrResult result = ocrService.recognize(file, OcrOptions.forResume());

            if (!result.isSuccess()) {
                log.error("简历识别失败: {}", result.getErrorMessage());
                return Result.error("识别失败: " + result.getErrorMessage());
            }

            // 提取结构化信息
            ResumeInfo resumeInfo = ocrHelper.extractResumeInfo(result.getText());

            Map<String, Object> response = new HashMap<>();
            response.put("rawText", result.getText());
            response.put("workYears", resumeInfo.getWorkYears());
            response.put("currentPosition", resumeInfo.getCurrentPosition());
            response.put("summary", resumeInfo.getSummary());
            response.put("confidence", result.getConfidence());
            response.put("processingTimeMs", result.getProcessingTimeMs());

            return Result.success(response);

        } catch (Exception e) {
            log.error("简历识别异常", e);
            return Result.error("识别失败: " + e.getMessage());
        }
    }

    /**
     * 上传并识别 JD
     * POST /api/ocr/jd
     *
     * @param file          JD 文件（支持PDF、DOCX、图片等）
     * @param applicationId 关联的申请ID（可选）
     * @return 识别结果
     */
    @PostMapping("/jd")
    public Result<Map<String, Object>> recognizeJD(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) Long applicationId
    ) {
        try {
            if (file.isEmpty()) {
                return Result.error("文件不能为空");
            }

            log.info("开始识别JD，文件名: {}, 文件大小: {} bytes", file.getOriginalFilename(), file.getSize());

            // 直接使用 MultipartFile 识别（支持PDF、DOCX、图片等）
            OcrResult result = ocrService.recognize(file, OcrOptions.forJD());

            if (!result.isSuccess()) {
                log.error("JD识别失败: {}", result.getErrorMessage());
                return Result.error("识别失败: " + result.getErrorMessage());
            }

            // 提取技能标签
            List<String> skills = ocrHelper.extractSkillsFromJD(result.getText());

            Map<String, Object> response = new HashMap<>();
            response.put("text", result.getText());
            response.put("skills", skills);
            response.put("confidence", result.getConfidence());
            response.put("processingTimeMs", result.getProcessingTimeMs());

            return Result.success(response);

        } catch (Exception e) {
            log.error("JD识别异常", e);
            return Result.error("识别失败: " + e.getMessage());
        }
    }

    /**
     * 获取当前用户的 OCR 调用记录
     * GET /api/ocr/records/my
     *
     * @return 记录列表
     */
    @GetMapping("/records/my")
    public Result<List<OcrCallRecord>> getMyRecords() {
        Long userId = UserContext.getCurrentUserId();
        List<OcrCallRecord> records = ocrRecordService.getByUserId(userId);
        return Result.success(records);
    }

    /**
     * 获取 OCR 调用记录（按会话筛选）
     * GET /api/ocr/records
     *
     * @param sessionId 会话 ID（可选）
     * @return 记录列表
     */
    @GetMapping("/records")
    public Result<List<OcrCallRecord>> getRecords(
            @RequestParam(required = false) String sessionId
    ) {
        List<OcrCallRecord> records;

        if (sessionId != null && !sessionId.isBlank()) {
            records = ocrRecordService.getBySessionId(sessionId);
        } else {
            // 返回当前用户的记录
            Long userId = UserContext.getCurrentUserIdOrDefault(null);
            if (userId != null) {
                records = ocrRecordService.getByUserId(userId);
            } else {
                // 未登录时返回空列表
                records = List.of();
            }
        }

        return Result.success(records);
    }

    /**
     * OCR 识别请求
     */
    @lombok.Data
    public static class OcrRecognizeRequest {
        /**
         * Base64 编码的图片数据
         */
        private String imageData;

        /**
         * 是否只识别表格
         */
        private Boolean tableOnly;
    }
}
