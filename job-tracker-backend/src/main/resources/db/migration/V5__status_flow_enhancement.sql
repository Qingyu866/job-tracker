-- =============================================
-- V5__status_flow_enhancement.sql
-- 状态流程增强迁移脚本
--
-- 功能：
-- 1. 添加面试轮次相关字段
-- 2. 扩展面试状态支持
-- 3. 创建状态变更审计日志表
-- =============================================

-- ========== 1. 面试记录表增强 ==========

-- 添加面试轮次字段（MySQL 不支持 IF NOT EXISTS，使用存储过程或直接添加）
SET @dbname = DATABASE();

-- 添加 round_number 列
SET @column_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'interview_records' AND COLUMN_NAME = 'round_number');

SET @sql = IF(@column_exists = 0,
    'ALTER TABLE interview_records ADD COLUMN round_number INT DEFAULT 1 COMMENT ''面试轮次（第几轮）'' AFTER interview_type',
    'SELECT 1');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加 is_final 列
SET @column_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'interview_records' AND COLUMN_NAME = 'is_final');

SET @sql = IF(@column_exists = 0,
    'ALTER TABLE interview_records ADD COLUMN is_final BOOLEAN DEFAULT FALSE COMMENT ''是否为终面'' AFTER round_number',
    'SELECT 1');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加 result 列
SET @column_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'interview_records' AND COLUMN_NAME = 'result');

SET @sql = IF(@column_exists = 0,
    'ALTER TABLE interview_records ADD COLUMN result VARCHAR(20) DEFAULT NULL COMMENT ''面试结果（PASSED/FAILED/PENDING）'' AFTER status',
    'SELECT 1');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ========== 2. 更新现有数据 ==========

-- 更新 HR 类型的面试为终面
UPDATE interview_records
SET is_final = TRUE
WHERE interview_type = 'HR' AND (is_final IS NULL OR is_final = FALSE);

-- 设置默认轮次（根据同一申请的时间排序）
UPDATE interview_records ir
JOIN (
    SELECT id,
    ROW_NUMBER() OVER (PARTITION BY application_id ORDER BY interview_date, created_at) as rn
    FROM interview_records
    WHERE deleted = 0
) ranked ON ir.id = ranked.id
SET ir.round_number = COALESCE(ir.round_number, ranked.rn);

-- ========== 3. 状态变更审计日志表 ==========

CREATE TABLE IF NOT EXISTS status_audit_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    entity_type VARCHAR(50) NOT NULL COMMENT '实体类型：APPLICATION/INTERVIEW',
    entity_id BIGINT NOT NULL COMMENT '实体ID',
    old_status VARCHAR(50) DEFAULT NULL COMMENT '原状态',
    new_status VARCHAR(50) NOT NULL COMMENT '新状态',
    reason VARCHAR(500) DEFAULT NULL COMMENT '变更原因',
    changed_by VARCHAR(100) DEFAULT NULL COMMENT '操作人',
    changed_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '变更时间',
    INDEX idx_entity (entity_type, entity_id),
    INDEX idx_changed_at (changed_at),
    INDEX idx_new_status (new_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='状态变更审计日志';

-- ========== 4. 面试进度视图（可选） ==========

CREATE OR REPLACE VIEW v_application_interview_progress AS
SELECT
    a.id AS application_id,
    a.company_id,
    a.job_title,
    a.status AS application_status,
    COUNT(i.id) AS total_interviews,
    SUM(CASE WHEN i.status = 'PASSED' THEN 1 ELSE 0 END) AS passed_interviews,
    SUM(CASE WHEN i.status = 'FAILED' THEN 1 ELSE 0 END) AS failed_interviews,
    SUM(CASE WHEN i.status IN ('SCHEDULED', 'IN_PROGRESS') THEN 1 ELSE 0 END) AS pending_interviews,
    MIN(CASE WHEN i.status IN ('SCHEDULED', 'IN_PROGRESS') THEN i.round_number ELSE NULL END) AS current_round
FROM job_applications a
LEFT JOIN interview_records i ON a.id = i.application_id AND i.deleted = 0
WHERE a.deleted = 0
GROUP BY a.id, a.company_id, a.job_title, a.status;
