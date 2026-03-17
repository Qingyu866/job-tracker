package com.jobtracker.auth.controller;

import com.jobtracker.common.ApiResponse;
import com.jobtracker.auth.dto.ChangePasswordRequest;
import com.jobtracker.auth.dto.LoginRequest;
import com.jobtracker.auth.dto.LoginResponse;
import com.jobtracker.auth.dto.RegisterRequest;
import com.jobtracker.auth.dto.UserInfo;
import com.jobtracker.auth.service.AuthService;
import cn.dev33.satoken.stp.StpUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 * <p>
 * 处理用户登录、注册、登出等认证相关接口
 * </p>
 *
 * @author Job Tracker Team
 * @since 2026-03-17
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ApiResponse.success("登录成功", response);
    }

    /**
     * 用户注册
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public ApiResponse<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ApiResponse.success("注册成功");
    }

    /**
     * 获取当前用户信息
     * GET /api/auth/info
     */
    @GetMapping("/info")
    public ApiResponse<UserInfo> getCurrentUser() {
        UserInfo userInfo = authService.getCurrentUserInfo();
        return ApiResponse.success(userInfo);
    }

    /**
     * 退出登录
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        StpUtil.logout();
        return ApiResponse.success("退出成功");
    }

    /**
     * 修改密码
     * POST /api/auth/change-password
     */
    @PostMapping("/change-password")
    public ApiResponse<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(request);
        return ApiResponse.success("密码修改成功");
    }
}
