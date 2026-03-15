-- ==========================================
-- Job Tracker Chat History Schema
-- Version: 2.0
-- Author: Job Tracker Team
-- Description: AI对话历史持久化表结构
-- ==========================================
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ==========================================
-- 表 1: chat_sessions (聊天会话表)
-- ==========================================
DROP TABLE IF EXISTS `chat_sessions`;
CREATE TABLE `chat_sessions` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '会话ID',
  `session_key` VARCHAR(100) NOT NULL COMMENT '会话唯一标识',
  `title` VARCHAR(200) DEFAULT NULL COMMENT '会话标题（可AI生成）',
  `message_count` INT NOT NULL DEFAULT 0 COMMENT '消息数量',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记（0:未删除, 1:已删除）',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_session_key` (`session_key`),
  INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天会话表';

-- ==========================================
-- 表 2: chat_messages (聊天消息表)
-- ==========================================
DROP TABLE IF EXISTS `chat_messages`;
CREATE TABLE `chat_messages` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `session_id` BIGINT UNSIGNED NOT NULL COMMENT '会话ID',
  `role` VARCHAR(20) NOT NULL COMMENT '角色: USER/ASSISTANT/SYSTEM',
  `content` TEXT NOT NULL COMMENT '消息内容',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  INDEX `idx_session_id` (`session_id`),
  INDEX `idx_created_at` (`created_at`),
  CONSTRAINT `fk_chat_messages_session` FOREIGN KEY (`session_id`) REFERENCES `chat_sessions` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天消息表';

-- ==========================================
-- 表 3: tool_call_records (工具调用记录表)
-- ==========================================
DROP TABLE IF EXISTS `tool_call_records`;
CREATE TABLE `tool_call_records` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `message_id` BIGINT UNSIGNED NOT NULL COMMENT '关联的AI消息ID',
  `tool_name` VARCHAR(100) NOT NULL COMMENT '工具名称',
  `tool_input` TEXT DEFAULT NULL COMMENT '工具入参(JSON)',
  `tool_output` TEXT DEFAULT NULL COMMENT '工具输出(JSON)',
  `status` VARCHAR(20) NOT NULL DEFAULT 'SUCCESS' COMMENT '状态: SUCCESS/FAILURE',
  `error_message` TEXT DEFAULT NULL COMMENT '错误信息',
  `execution_time_ms` INT DEFAULT NULL COMMENT '执行耗时(毫秒)',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  INDEX `idx_message_id` (`message_id`),
  INDEX `idx_tool_name` (`tool_name`),
  CONSTRAINT `fk_tool_call_records_message` FOREIGN KEY (`message_id`) REFERENCES `chat_messages` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='工具调用记录表';

SET FOREIGN_KEY_CHECKS = 1;
