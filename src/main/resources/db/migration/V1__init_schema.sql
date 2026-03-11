-- ==========================================
-- Job Tracker Database Schema
-- Version: 1.0
-- Author: Job Tracker Team
-- Description: 求职追踪系统数据库初始化脚本
-- ==========================================
-- 设置字符集
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ==========================================
-- 表 1: companies (公司信息表)
-- ==========================================
DROP TABLE IF EXISTS `companies`;
CREATE TABLE `companies` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '公司ID',
  `name` VARCHAR(255) NOT NULL COMMENT '公司名称',
  `industry` VARCHAR(100) DEFAULT NULL COMMENT '行业类型',
  `size` VARCHAR(50) DEFAULT NULL COMMENT '公司规模（如：100-499人）',
  `location` VARCHAR(255) DEFAULT NULL COMMENT '公司地址',
  `website` VARCHAR(500) DEFAULT NULL COMMENT '公司官网',
  `description` TEXT DEFAULT NULL COMMENT '公司描述',
  `logo_url` VARCHAR(500) DEFAULT NULL COMMENT '公司Logo URL',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记（0:未删除, 1:已删除）',
  PRIMARY KEY (`id`),
  INDEX `idx_name` (`name`),
  INDEX `idx_industry` (`industry`),
  INDEX `idx_location` (`location`),
  INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公司信息表';

-- ==========================================
-- 表 2: job_applications (求职申请表)
-- ==========================================
DROP TABLE IF EXISTS `job_applications`;
CREATE TABLE `job_applications` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '申请ID',
  `company_id` BIGINT UNSIGNED NOT NULL COMMENT '公司ID',
  `job_title` VARCHAR(255) NOT NULL COMMENT '职位名称',
  `job_description` TEXT DEFAULT NULL COMMENT '职位描述',
  `job_type` VARCHAR(50) DEFAULT NULL COMMENT '工作类型（全职/兼职/实习/合同）',
  `work_location` VARCHAR(255) DEFAULT NULL COMMENT '工作地点',
  `salary_min` DECIMAL(12,2) DEFAULT NULL COMMENT '薪资下限',
  `salary_max` DECIMAL(12,2) DEFAULT NULL COMMENT '薪资上限',
  `salary_currency` VARCHAR(10) DEFAULT 'CNY' COMMENT '薪资货币',
  `job_url` VARCHAR(1000) DEFAULT NULL COMMENT '职位链接（如：招聘网站URL）',
  `status` VARCHAR(50) NOT NULL DEFAULT 'WISHLIST' COMMENT '申请状态（WISHLIST/APPLIED/INTERVIEW/OFFER/REJECTED/WITHDRAWN）',
  `application_date` DATE DEFAULT NULL COMMENT '申请日期',
  `notes` TEXT DEFAULT NULL COMMENT '备注信息',
  `priority` TINYINT DEFAULT 5 COMMENT '优先级（1-10，10最高）',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记（0:未删除, 1:已删除）',
  PRIMARY KEY (`id`),
  INDEX `idx_company_id` (`company_id`),
  INDEX `idx_status` (`status`),
  INDEX `idx_application_date` (`application_date`),
  INDEX `idx_priority` (`priority`),
  INDEX `idx_created_at` (`created_at`),
  CONSTRAINT `fk_job_applications_company` FOREIGN KEY (`company_id`) REFERENCES `companies` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='求职申请表';

-- ==========================================
-- 表 3: interview_records (面试记录表)
-- ==========================================
DROP TABLE IF EXISTS `interview_records`;
CREATE TABLE `interview_records` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '面试记录ID',
  `application_id` BIGINT UNSIGNED NOT NULL COMMENT '申请ID',
  `interview_type` VARCHAR(50) NOT NULL COMMENT '面试类型（PHONE/VIDEO/ONSITE/TECHNICAL/HR）',
  `interview_date` DATETIME NOT NULL COMMENT '面试时间',
  `interviewer_name` VARCHAR(255) DEFAULT NULL COMMENT '面试官姓名',
  `interviewer_title` VARCHAR(255) DEFAULT NULL COMMENT '面试官职位',
  `duration_minutes` INT DEFAULT NULL COMMENT '面试时长（分钟）',
  `status` VARCHAR(50) NOT NULL DEFAULT 'SCHEDULED' COMMENT '面试状态（SCHEDULED/COMPLETED/CANCELLED/NO_SHOW）',
  `rating` TINYINT DEFAULT NULL COMMENT '面试评分（1-5分）',
  `feedback` TEXT DEFAULT NULL COMMENT '面试反馈',
  `technical_questions` TEXT DEFAULT NULL COMMENT '技术问题记录',
  `notes` TEXT DEFAULT NULL COMMENT '备注信息',
  `follow_up_required` TINYINT(1) DEFAULT 0 COMMENT '是否需要跟进（0:否, 1:是）',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记（0:未删除, 1:已删除）',
  PRIMARY KEY (`id`),
  INDEX `idx_application_id` (`application_id`),
  INDEX `idx_interview_date` (`interview_date`),
  INDEX `idx_status` (`status`),
  INDEX `idx_created_at` (`created_at`),
  CONSTRAINT `fk_interview_records_application` FOREIGN KEY (`application_id`) REFERENCES `job_applications` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='面试记录表';

-- ==========================================
-- 表 4: application_logs (申请日志表)
-- ==========================================
DROP TABLE IF EXISTS `application_logs`;
CREATE TABLE `application_logs` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `application_id` BIGINT UNSIGNED NOT NULL COMMENT '申请ID',
  `log_type` VARCHAR(50) NOT NULL COMMENT '日志类型（STATUS_CHANGE/INTERVIEW_SCHEDULED/FEEDBACK_RECEIVED/NOTE_ADDED/DOCUMENT_UPLOADED）',
  `log_title` VARCHAR(255) NOT NULL COMMENT '日志标题',
  `log_content` TEXT DEFAULT NULL COMMENT '日志内容',
  `logged_by` VARCHAR(100) DEFAULT 'SYSTEM' COMMENT '记录者（SYSTEM/USER）',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记（0:未删除, 1:已删除）',
  PRIMARY KEY (`id`),
  INDEX `idx_application_id` (`application_id`),
  INDEX `idx_log_type` (`log_type`),
  INDEX `idx_created_at` (`created_at`),
  CONSTRAINT `fk_application_logs_application` FOREIGN KEY (`application_id`) REFERENCES `job_applications` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='申请日志表';

-- ==========================================
-- 初始化数据
-- ==========================================

-- 插入示例公司数据
INSERT INTO `companies` (`name`, `industry`, `size`, `location`, `website`, `description`) VALUES
('阿里巴巴集团', '互联网', '10000+人', '杭州', 'https://www.alibaba.com', '全球领先的电子商务和科技公司'),
('腾讯控股', '互联网', '10000+人', '深圳', 'https://www.tencent.com', '中国领先的互联网增值服务提供商'),
('字节跳动', '互联网', '10000+人', '北京', 'https://www.bytedance.com', '全球化的科技公司'),
('美团', '互联网', '10000+人', '北京', 'https://www.meituan.com', '中国领先的本地生活服务平台'),
('京东集团', '电商', '10000+人', '北京', 'https://www.jd.com', '中国最大的电商平台之一');

SET FOREIGN_KEY_CHECKS = 1;

-- ==========================================
-- 表结构验证查询
-- ==========================================
-- 查看所有表
-- SHOW TABLES;
--
-- 查看表结构
-- DESC companies;
-- DESC job_applications;
-- DESC interview_records;
-- DESC application_logs;
--
-- 查看外键关系
-- SELECT
--   TABLE_NAME,
--   COLUMN_NAME,
--   CONSTRAINT_NAME,
--   REFERENCED_TABLE_NAME,
--   REFERENCED_COLUMN_NAME
-- FROM information_schema.KEY_COLUMN_USAGE
-- WHERE TABLE_SCHEMA = 'job_tracker'
--   AND REFERENCED_TABLE_NAME IS NOT NULL;
-- ==========================================
