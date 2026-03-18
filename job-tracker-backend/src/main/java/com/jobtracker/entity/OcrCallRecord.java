package com.jobtracker.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * OCR 调用记录实体
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 2026-03-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("ocr_call_records")
public class OcrCallRecord {

    /**
     * 记录 ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long recordId;

    /**
     * 关联的聊天会话
     */
    private String sessionId;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 图片 URL（OSS 存储）
     */
    private String imageUrl;

    /**
     * 图片类型：RESUME/JD/GENERAL
     */
    private String imageType;

    /**
     * OCR 提供商
     */
    private String ocrProvider;

    /**
     * 请求图片大小（字节）
     */
    private Integer requestSizeBytes;

    /**
     * 识别状态：SUCCESS/FAILED/FALLBACK
     */
    private String ocrStatus;

    /**
     * 识别的文本内容
     */
    private String recognizedText;

    /**
     * 置信度（0.00-1.00）
     */
    private BigDecimal confidenceScore;

    /**
     * 处理耗时（毫秒）
     */
    private Integer processingTimeMs;

    /**
     * 错误代码
     */
    private String errorCode;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
