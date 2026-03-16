-- V7__multimodal_support.sql
-- 多模态聊天支持：图片上传和存储
-- 创建日期：2026-03-15

-- 1. 创建独立的聊天图片表
CREATE TABLE IF NOT EXISTS chat_images (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '图片ID',
    message_id BIGINT NOT NULL COMMENT '关联的消息ID',
    session_id BIGINT NOT NULL COMMENT '所属会话ID（冗余字段，用于安全校验）',
    file_path VARCHAR(500) NOT NULL COMMENT '服务器内部相对路径',
    mime_type VARCHAR(100) NOT NULL COMMENT 'MIME 类型',
    file_name VARCHAR(255) COMMENT '原始文件名',
    file_size BIGINT COMMENT '文件大小（字节）',
    width INT COMMENT '图片宽度',
    height INT COMMENT '图片高度',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',

    INDEX idx_message_id (message_id),
    INDEX idx_session_id (session_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天图片表';

-- 2. 添加索引优化 chat_messages 查询
-- 注意：如果索引已存在会报错，但不影响数据，可以忽略
ALTER TABLE chat_messages
ADD INDEX idx_chat_messages_session_created (session_id, created_at DESC);

