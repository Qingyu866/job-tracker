package com.jobtracker.ocr.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jobtracker.entity.OcrCallRecord;
import com.jobtracker.ocr.dto.OcrResult;
import com.jobtracker.mapper.OcrCallRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * OCR 记录服务
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 2026-03-16
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OcrRecordService {

    private final OcrCallRecordMapper recordMapper;

    /**
     * 保存 OCR 调用记录
     *
     * @param sessionId      会话 ID
     * @param userId         用户 ID
     * @param imageUrl       图片 URL
     * @param imageType      图片类型
     * @param ocrResult      OCR 识别结果
     * @param imageSizeBytes 图片大小
     * @return 保存的记录
     */
    public OcrCallRecord saveRecord(
            String sessionId,
            Long userId,
            String imageUrl,
            String imageType,
            OcrResult ocrResult,
            Integer imageSizeBytes
    ) {
        OcrCallRecord record = OcrCallRecord.builder()
                .sessionId(sessionId)
                .userId(userId)
                .imageUrl(imageUrl)
                .imageType(imageType)
                .ocrProvider(ocrResult.getProvider())
                .requestSizeBytes(imageSizeBytes)
                .ocrStatus(ocrResult.getStatus().name())
                .recognizedText(ocrResult.getText())
                .confidenceScore(ocrResult.getConfidence() != null
                        ? BigDecimal.valueOf(ocrResult.getConfidence())
                        : null)
                .processingTimeMs(ocrResult.getProcessingTimeMs() != null
                        ? ocrResult.getProcessingTimeMs().intValue()
                        : null)
                .errorCode(ocrResult.getErrorCode())
                .errorMessage(ocrResult.getErrorMessage())
                .createdAt(LocalDateTime.now())
                .build();

        recordMapper.insert(record);
        log.debug("保存 OCR 记录，ID: {}", record.getRecordId());
        return record;
    }

    /**
     * 根据会话 ID 获取记录列表
     *
     * @param sessionId 会话 ID
     * @return 记录列表
     */
    public java.util.List<OcrCallRecord> getBySessionId(String sessionId) {
        return recordMapper.selectList(
                new LambdaQueryWrapper<OcrCallRecord>()
                        .eq(OcrCallRecord::getSessionId, sessionId)
                        .orderByDesc(OcrCallRecord::getCreatedAt)
        );
    }

    /**
     * 根据用户 ID 获取记录列表
     *
     * @param userId 用户 ID
     * @return 记录列表
     */
    public java.util.List<OcrCallRecord> getByUserId(Long userId) {
        return recordMapper.selectList(
                new LambdaQueryWrapper<OcrCallRecord>()
                        .eq(OcrCallRecord::getUserId, userId)
                        .orderByDesc(OcrCallRecord::getCreatedAt)
        );
    }

    /**
     * 统计用户今日 OCR 调用次数
     *
     * @param userId 用户 ID
     * @return 今日调用次数
     */
    public long countTodayCalls(Long userId) {
        LocalDateTime todayStart = LocalDateTime.now().toLocalDate().atStartOfDay();
        return recordMapper.selectCount(
                new LambdaQueryWrapper<OcrCallRecord>()
                        .eq(OcrCallRecord::getUserId, userId)
                        .ge(OcrCallRecord::getCreatedAt, todayStart)
        );
    }
}
