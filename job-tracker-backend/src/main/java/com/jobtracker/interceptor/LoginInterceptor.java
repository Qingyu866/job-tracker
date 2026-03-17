package com.jobtracker.interceptor;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.session.SaSession;
import com.jobtracker.context.UserContext;
import com.jobtracker.context.UserInfoHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录拦截器
 * <p>
 * 功能：
 * 1. 在请求开始时，如果用户已登录，将用户信息设置到 UserContext
 * 2. 在请求结束时，清理 UserContext 防止内存泄漏
 * </p>
 *
 * @author Job Tracker Team
 * @since 2026-03-17
 */
@Slf4j
@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 如果已登录，设置用户上下文
        if (StpUtil.isLogin()) {
            try {
                Long userId = StpUtil.getLoginIdAsLong();
                SaSession session = StpUtil.getSession();

                UserInfoHolder holder = UserInfoHolder.builder()
                        .userId(userId)
                        .username(session.getString("username"))
                        .nickname(session.getString("nickname"))
                        .avatar(session.getString("avatar"))
                        .email(session.getString("email"))
                        .phone(session.getString("phone"))
                        .build();

                UserContext.setCurrentUser(holder);

                log.debug("设置用户上下文: userId={}, username={}", userId, holder.getUsername());
            } catch (Exception e) {
                log.warn("设置用户上下文失败: {}", e.getMessage());
            }
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        // 请求结束时清理 ThreadLocal
        UserContext.clear();
    }
}
