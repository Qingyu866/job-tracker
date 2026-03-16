-- V8__ocr_support.sql
-- OCR 功能支持
-- 创建日期: 2026-03-16

-- 1. 创建 OCR 调用记录表
CREATE TABLE IF NOT EXISTS ocr_call_records (
    record_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id VARCHAR(100) COMMENT '关联的聊天会话',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    image_url VARCHAR(500) COMMENT '图片URL（OSS存储）',
    image_type VARCHAR(20) COMMENT '图片类型：RESUME/JD/GENERAL',
    ocr_provider VARCHAR(50) DEFAULT 'ZHIPU' COMMENT 'OCR提供商',
    request_size_bytes INT COMMENT '请求图片大小（字节）',
    ocr_status VARCHAR(20) COMMENT '识别状态：SUCCESS/FAILED/FALLBACK',
    recognized_text TEXT COMMENT '识别的文本内容',
    confidence_score DECIMAL(3,2) COMMENT '置信度（0.00-1.00）',
    processing_time_ms INT COMMENT '处理耗时（毫秒）',
    error_code VARCHAR(50) COMMENT '错误代码',
    error_message VARCHAR(500) COMMENT '错误信息',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_session (session_id),
    INDEX idx_user (user_id),
    INDEX idx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
COMMENT='OCR调用记录表';

-- 2. 创建 OCR 使用配额表（可选，用于计费控制）
CREATE TABLE IF NOT EXISTS ocr_quota (
    user_id BIGINT PRIMARY KEY,
    total_quota INT DEFAULT 100 COMMENT '总配额（每月）',
    used_quota INT DEFAULT 0 COMMENT '已使用',
    reset_date DATE COMMENT '重置日期',
    INDEX idx_reset (reset_date)
) COMMENT='OCR使用配额表';
