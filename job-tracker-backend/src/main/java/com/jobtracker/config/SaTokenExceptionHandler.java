package com.jobtracker.config;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import com.jobtracker.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Sa-Token 全局异常处理器
 * <p>
 * 处理 Sa-Token 抛出的各类异常
 * </p>
 *
 * @author Job Tracker Team
 * @since 2026-03-17
 */
@Slf4j
@RestControllerAdvice
public class SaTokenExceptionHandler {

    /**
     * Sa-Token 未登录异常
     */
    @ExceptionHandler(NotLoginException.class)
    public Result<Void> handleNotLogin(NotLoginException e) {
        String message = switch (e.getType()) {
            case NotLoginException.NOT_TOKEN -> "未提供 Token";
            case NotLoginException.INVALID_TOKEN -> "Token 无效";
            case NotLoginException.TOKEN_TIMEOUT -> "Token 已过期";
            case NotLoginException.BE_REPLACED -> "Token 已被顶下线";
            case NotLoginException.KICK_OUT -> "Token 已被踢下线";
            default -> "未登录";
        };

        log.warn("未登录访问: {}", message);
        return Result.error(-1, message);
    }

    /**
     * Sa-Token 权限异常
     */
    @ExceptionHandler(NotPermissionException.class)
    public Result<Void> handleNotPermission(NotPermissionException e) {
        log.warn("权限不足: {}", e.getPermission());
        return Result.error(-1, "权限不足: " + e.getPermission());
    }

    /**
     * Sa-Token 角色异常
     */
    @ExceptionHandler(NotRoleException.class)
    public Result<Void> handleNotRole(NotRoleException e) {
        log.warn("角色权限不足: {}", e.getRole());
        return Result.error(-1, "需要角色: " + e.getRole());
    }
}
