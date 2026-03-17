-- V10__interview_agent_memory.sql
-- 面试 Agent 独立记忆存储
-- 创建日期: 2026-03-17
-- 说明：实现两层隔离的面试 Agent 记忆系统

-- ================================
-- 面试记忆存储表
-- ================================
CREATE TABLE IF NOT EXISTS interview_memories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记忆ID',
    session_id VARCHAR(64) NOT NULL UNIQUE COMMENT '模拟面试会话ID（对应 mock_interview_sessions.session_id）',

    -- 三个 Agent 的独立记忆（JSON 存储）
    main_interviewer_memory JSON COMMENT '主面试官记忆：提问历史、用户回答、策略',
    vice_interviewer_memory JSON COMMENT '副面试官记忆：选题决策、考察进度',
    evaluator_memory JSON COMMENT '评审专家记忆：评分历史、真实性分析',

    -- 元数据
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    INDEX idx_session_id (session_id),
    INDEX idx_updated_at (updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='面试 Agent 独立记忆存储（两层隔离：会话间 + Agent间）';

-- ================================
-- 隔离说明
-- ================================
-- 第一层：会话间隔离
--   不同 session_id 的记录完全独立
--   例如：session-byt-001（字节跳动面试）与 session-tx-002（腾讯面试）互不干扰
--
-- 第二层：Agent 间隔离
--   同一 session_id 的三个 Agent 记忆存储在不同的 JSON 字段中
--   例如：主面试官只能看到 main_interviewer_memory，无法看到其他两个 Agent 的记忆
