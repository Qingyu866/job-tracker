-- V11: 合并考察计划功能到评分表
-- 创建日期: 2026-03-19
-- 目的: 将考察计划功能合并到 mock_interview_evaluations 表中，避免表冗余
-- 基于文档: docs/功能设计/20260319_考察计划与评分表关系分析_避免冗余设计.md

-- =====================================================
-- 1. 添加考察计划相关字段到 mock_interview_evaluations
-- =====================================================

ALTER TABLE mock_interview_evaluations
    ADD COLUMN plan_status VARCHAR(20) DEFAULT 'PENDING' COMMENT '计划状态：PENDING(待考察), IN_PROGRESS(执行中), COMPLETED(已完成), SKIPPED(跳过), PAUSED(暂停中)';

ALTER TABLE mock_interview_evaluations
    ADD COLUMN topic_source VARCHAR(50) COMMENT '选题来源：PROJECT_DEEP_DIVE, SKILL_VERIFICATION, JD_REQUIREMENT, GENERAL';

ALTER TABLE mock_interview_evaluations
    ADD COLUMN question_type VARCHAR(50) COMMENT '问题类型：PROJECT_DEEP_DIVE, SKILL_VERIFICATION, JD_REQUIREMENT, GENERAL, OPEN_ENDED';

ALTER TABLE mock_interview_evaluations
    ADD COLUMN planned_difficulty INT COMMENT '计划难度（1-5）';

ALTER TABLE mock_interview_evaluations
    ADD COLUMN actual_difficulty INT COMMENT '实际难度（可能与计划不同）';

ALTER TABLE mock_interview_evaluations
    ADD COLUMN context_info TEXT COMMENT '上下文信息（例如：简历声称精通Redis）';

ALTER TABLE mock_interview_evaluations
    ADD COLUMN reason TEXT COMMENT '选择该技能的原因';

-- =====================================================
-- 2. 添加索引优化查询性能
-- =====================================================

-- 用于快速查询某个会话的待考察计划
CREATE INDEX idx_eval_session_status
    ON mock_interview_evaluations(session_id, plan_status);

-- 用于按轮次和技能查询
CREATE INDEX idx_eval_round_skill
    ON mock_interview_evaluations(round_number, skill_name);

-- 用于查询某个会话的所有轮次
CREATE INDEX idx_eval_session_round
    ON mock_interview_evaluations(session_id, round_number);

-- =====================================================
-- 3. 添加会话进度追踪字段到 mock_interview_sessions
-- =====================================================

ALTER TABLE mock_interview_sessions
    ADD COLUMN current_plan_id BIGINT COMMENT '当前执行的计划ID（用于暂停/恢复）';

ALTER TABLE mock_interview_sessions
    ADD COLUMN total_plans INT DEFAULT 0 COMMENT '总考察计划数量';

ALTER TABLE mock_interview_sessions
    ADD COLUMN completed_plans INT DEFAULT 0 COMMENT '已完成计划数量';

ALTER TABLE mock_interview_sessions
    ADD COLUMN paused_at TIMESTAMP NULL COMMENT '暂停时间';

ALTER TABLE mock_interview_sessions
    ADD COLUMN resumed_at TIMESTAMP NULL COMMENT '恢复时间';

-- =====================================================
-- 4. 添加状态枚举约束（可选，MySQL 8.0.16+）
-- =====================================================

-- MySQL 8.0.16+ 支持检查约束
-- ALTER TABLE mock_interview_evaluations
--     ADD CONSTRAINT chk_plan_status
--     CHECK (plan_status IN ('PENDING', 'IN_PROGRESS', 'COMPLETED', 'SKIPPED', 'PAUSED'));

-- =====================================================
-- 5. 数据迁移说明
-- =====================================================

-- 如果已有数据，需要将现有记录的状态设置为 COMPLETED
-- UPDATE mock_interview_evaluations
-- SET plan_status = 'COMPLETED'
-- WHERE plan_status IS NULL;

-- =====================================================
-- 6. 验证脚本
-- =====================================================

-- 查看表结构
-- DESC mock_interview_evaluations;

-- 查看索引
-- SHOW INDEX FROM mock_interview_evaluations;

-- 查看会话表结构
-- DESC mock_interview_sessions;

-- =====================================================
-- 7. 回滚脚本（如果需要）
-- =====================================================

/*
-- 回滚 mock_interview_evaluations 表修改
ALTER TABLE mock_interview_evaluations
    DROP COLUMN plan_status,
    DROP COLUMN topic_source,
    DROP COLUMN question_type,
    DROP COLUMN planned_difficulty,
    DROP COLUMN actual_difficulty,
    DROP COLUMN context_info,
    DROP COLUMN reason;

-- 回滚 mock_interview_sessions 表修改
ALTER TABLE mock_interview_sessions
    DROP COLUMN current_plan_id,
    DROP COLUMN total_plans,
    DROP COLUMN completed_plans,
    DROP COLUMN paused_at,
    DROP COLUMN resumed_at;

-- 删除索引
DROP INDEX idx_eval_session_status ON mock_interview_evaluations;
DROP INDEX idx_eval_round_skill ON mock_interview_evaluations;
DROP INDEX idx_eval_session_round ON mock_interview_evaluations;
*/
