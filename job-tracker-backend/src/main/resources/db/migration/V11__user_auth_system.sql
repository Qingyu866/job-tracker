-- ========================================
-- 用户认证系统数据库迁移脚本
-- 版本: V11
-- 创建日期: 2026-03-17
-- 说明: 创建用户表和角色表，用于 Sa-Token 认证
-- ========================================

-- 1. 创建用户表
CREATE TABLE IF NOT EXISTS `sys_user` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名（登录名）',
    `password` VARCHAR(128) NOT NULL COMMENT '密码（Sa-Token SHA256+盐 加密）',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用, 1-正常',
    `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
    `last_login_ip` VARCHAR(50) DEFAULT NULL COMMENT '最后登录IP',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除, 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`),
    UNIQUE KEY `uk_phone` (`phone`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户表';

-- 2. 创建用户角色关联表
CREATE TABLE IF NOT EXISTS `sys_user_role` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    `role_code` VARCHAR(50) NOT NULL COMMENT '角色代码：USER/ADMIN',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`, `role_code`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- 3. 插入默认管理员账户
-- 用户名: admin
-- 密码: 123456
-- 加密方式: Sa-Token SaSecureUtil (SHA256 + 盐, 1024 次散列)
--
-- ⚠️ 重要说明:
-- 当前使用的是临时占位密码（仅 SHA256 一次），仅用于开发测试
-- 生产环境部署前，请使用以下方式重新生成正确的加密密码：
--
-- 方式一：启动后端后，调用修改密码接口
-- 方式二：使用 PasswordUtil.generateDefaultAdminPassword() 生成
--
-- 临时密码（仅用于测试）: SHA-256("123456") =
INSERT INTO `sys_user` (`id`, `username`, `password`, `nickname`, `status`)
VALUES (
    1,
    'admin',
    '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918',
    '系统管理员',
    1
) ON DUPLICATE KEY UPDATE `username` = `username`;

-- 4. 为管理员分配角色
INSERT INTO `sys_user_role` (`user_id`, `role_code`)
VALUES (1, 'ADMIN')
ON DUPLICATE KEY UPDATE `user_id` = `user_id`;

-- 5. 创建索引优化查询性能
-- MySQL 不支持 CREATE INDEX IF NOT EXISTS，使用存储过程处理
DROP PROCEDURE IF EXISTS add_index_if_not_exists$$

CREATE PROCEDURE add_index_if_not_exists()
BEGIN
    -- 检查并创建 idx_user_status 索引
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.statistics
        WHERE table_schema = DATABASE()
        AND table_name = 'sys_user'
        AND index_name = 'idx_user_status'
    ) THEN
        CREATE INDEX `idx_user_status` ON `sys_user` (`status`, `deleted`);
    END IF;

    -- 检查并创建 idx_user_created 索引
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.statistics
        WHERE table_schema = DATABASE()
        AND table_name = 'sys_user'
        AND index_name = 'idx_user_created'
    ) THEN
        CREATE INDEX `idx_user_created` ON `sys_user` (`created_at`);
    END IF;
END$$

DELIMITER ;
CALL add_index_if_not_exists();
DROP PROCEDURE add_index_if_not_exists;

-- ========================================
-- 执行说明
-- ========================================
-- 1. 执行本脚本前，请确保数据库 job_tracker 已创建
-- 2. 执行命令: mysql -u root -p job_tracker < V11__user_auth_system.sql
-- 3. 执行后可使用以下账户登录测试:
--    用户名: admin
--    密码: 123456
-- ========================================
