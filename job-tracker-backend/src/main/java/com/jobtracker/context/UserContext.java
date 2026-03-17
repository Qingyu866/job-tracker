package com.jobtracker.context;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.session.SaSession;

/**
 * 用户上下文 - 存储当前请求的用户信息
 * <p>
 * 类似 Spring Security 的 SecurityContextHolder
 * 使用 ThreadLocal 存储当前请求的用户信息
 * </p>
 *
 * @author Job Tracker Team
 * @since 2026-03-17
 */
public class UserContext {

    private static final ThreadLocal<UserInfoHolder> CONTEXT_HOLDER = new ThreadLocal<>();

    /**
     * 设置当前用户信息
     *
     * @param userInfo 用户信息
     */
    public static void setCurrentUser(UserInfoHolder userInfo) {
        CONTEXT_HOLDER.set(userInfo);
    }

    /**
     * 获取当前用户信息
     * <p>
     * 如果 ThreadLocal 中没有，则从 Sa-Token Session 中获取
     * </p>
     *
     * @return 用户信息，未登录返回 null
     */
    public static UserInfoHolder getCurrentUser() {
        UserInfoHolder holder = CONTEXT_HOLDER.get();
        if (holder == null && StpUtil.isLogin()) {
            // 从 Sa-Token Session 获取并缓存到 ThreadLocal
            Long userId = StpUtil.getLoginIdAsLong();
            SaSession session = StpUtil.getSession();

            holder = UserInfoHolder.builder()
                    .userId(userId)
                    .username(session.getString("username"))
                    .nickname(session.getString("nickname"))
                    .avatar(session.getString("avatar"))
                    .build();

            CONTEXT_HOLDER.set(holder);
        }
        return holder;
    }

    /**
     * 获取当前用户 ID
     *
     * @return 用户 ID
     * @throws cn.dev33.satoken.exception.NotLoginException 未登录时抛出异常
     */
    public static Long getCurrentUserId() {
        UserInfoHolder holder = getCurrentUser();
        if (holder == null || holder.getUserId() == null) {
            throw new cn.dev33.satoken.exception.NotLoginException(
                    "未登录或登录已过期",
                    cn.dev33.satoken.exception.NotLoginException.NOT_TOKEN,
                    null
            );
        }
        return holder.getUserId();
    }

    /**
     * 获取当前用户 ID（带默认值）
     * <p>
     * 未登录时返回默认值，不抛异常
     * </p>
     *
     * @param defaultValue 默认值
     * @return 用户 ID 或默认值
     */
    public static Long getCurrentUserIdOrDefault(Long defaultValue) {
        try {
            return getCurrentUserId();
        } catch (cn.dev33.satoken.exception.NotLoginException e) {
            return defaultValue;
        }
    }

    /**
     * 获取当前用户名
     *
     * @return 用户名
     */
    public static String getCurrentUsername() {
        UserInfoHolder holder = getCurrentUser();
        return holder != null ? holder.getUsername() : null;
    }

    /**
     * 判断是否已登录
     *
     * @return true-已登录, false-未登录
     */
    public static boolean isLoggedIn() {
        return StpUtil.isLogin();
    }

    /**
     * 获取当前 Token
     *
     * @return Token 字符串
     */
    public static String getToken() {
        return StpUtil.getTokenValue();
    }

    /**
     * 清除当前用户信息（请求结束时调用）
     * <p>
     * 防止 ThreadLocal 内存泄漏
     * </p>
     */
    public static void clear() {
        CONTEXT_HOLDER.remove();
    }
}
