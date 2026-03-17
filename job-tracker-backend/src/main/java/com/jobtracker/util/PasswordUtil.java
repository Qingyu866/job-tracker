package com.jobtracker.util;

import cn.dev33.satoken.secure.SaSecureUtil;

import java.util.Random;
import java.util.UUID;

/**
 * 密码加密工具类 - 使用 Sa-Token 的 SaSecureUtil
 * <p>
 * 加密策略：SHA256 + 盐
 * - 盐值：用户名 + 固定盐
 * - 散列次数：1024 次
 * </p>
 *
 * @author Job Tracker Team
 * @since 2026-03-17
 */
public class PasswordUtil {

    /**
     * 固定盐值（生产环境应配置在配置文件中）
     */
    private static final String SALT = "job_tracker_sa_token_salt_2026";

    /**
     * 散列次数
     */
    private static final int HASH_ITERATIONS = 1024;

    /**
     * 加密密码
     *
     * @param password 明文密码
     * @param username 用户名（作为盐的一部分）
     * @return 加密后的密码
     */
    public static String encrypt(String password, String username) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }

        // 1. 生成组合盐：用户名 + 固定盐
        String combinedSalt = username + SALT;

        // 2. 第一次 SHA256 加密
        String hash1 = SaSecureUtil.sha256(password + combinedSalt);

        // 3. 多次散列增强安全性
        String hash = hash1;
        for (int i = 0; i < HASH_ITERATIONS; i++) {
            hash = SaSecureUtil.sha256(hash + combinedSalt);
        }

        return hash;
    }

    /**
     * 验证密码
     *
     * @param password        明文密码
     * @param username        用户名
     * @param hashedPassword 数据库中存储的加密密码
     * @return 是否匹配
     */
    public static boolean matches(String password, String username, String hashedPassword) {
        if (password == null || hashedPassword == null) {
            return false;
        }
        String encrypted = encrypt(password, username);
        return encrypted.equals(hashedPassword);
    }

    /**
     * 生成随机盐（预留接口）
     */
    public static String generateSalt() {
        return UUID.randomUUID().toString();
    }

    /**
     * 生成安全的随机密码
     *
     * @param length 密码长度
     * @return 随机密码
     */
    public static String generateRandomPassword(int length) {
        if (length < 4) {
            length = 8;
        }

        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        StringBuilder password = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < length; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }

        return password.toString();
    }

    /**
     * 生成默认管理员密码的加密值
     * 用于数据库初始化
     */
    public static String generateDefaultAdminPassword() {
        return encrypt("123456", "admin");
    }
}
