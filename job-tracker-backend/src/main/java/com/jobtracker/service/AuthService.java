package com.jobtracker.service;

import cn.dev33.satoken.secure.SaSecureUtil;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jobtracker.context.UserContext;
import com.jobtracker.dto.ChangePasswordRequest;
import com.jobtracker.dto.LoginRequest;
import com.jobtracker.dto.LoginResponse;
import com.jobtracker.dto.RegisterRequest;
import com.jobtracker.dto.UserInfo;
import com.jobtracker.util.PasswordUtil;
import com.jobtracker.entity.SysUser;
import com.jobtracker.common.exception.BusinessException;
import com.jobtracker.mapper.SysUserMapper;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 认证服务
 * <p>
 * 处理用户登录、注册、密码修改等认证相关功能
 * </p>
 *
 * @author Job Tracker Team
 * @since 2026-03-17
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserMapper userMapper;

    /**
     * 用户登录
     */
    @Transactional
    public LoginResponse login(LoginRequest request) {
        // 1. 查询用户
        SysUser user = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, request.getUsername())
        );

        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }

        // 2. 验证密码（使用 Sa-Token 加密方式）
        if (!PasswordUtil.matches(request.getPassword(), request.getUsername(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        // 3. 检查状态
        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }

        // 4. 执行登录
        StpUtil.login(user.getId());

        // 5. 将用户信息存入 Session (true 参数表示如果 session 不存在则创建)
        SaSession session = StpUtil.getSession(true);
        // ConcurrentHashMap 不允许 null 值，需要先检查
        if (user.getUsername() != null) {
            session.set("username", user.getUsername());
        }
        if (user.getNickname() != null) {
            session.set("nickname", user.getNickname());
        }
        if (user.getAvatar() != null) {
            session.set("avatar", user.getAvatar());
        }
        if (user.getEmail() != null) {
            session.set("email", user.getEmail());
        }
        if (user.getPhone() != null) {
            session.set("phone", user.getPhone());
        }

        // 6. 更新登录信息
        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp(StpUtil.getTokenInfo().getLoginDevice());
        userMapper.updateById(user);

        log.info("用户登录成功: userId={}, username={}", user.getId(), user.getUsername());

        // 7. 返回结果
        return LoginResponse.builder()
                .token(StpUtil.getTokenValue())
                .tokenName(StpUtil.getTokenName())
                .userInfo(buildUserInfo(user))
                .build();
    }

    /**
     * 用户注册
     */
    @Transactional
    public void register(RegisterRequest request) {
        // 1. 校验用户名
        if (request.getUsername() == null || request.getUsername().length() < 3) {
            throw new BusinessException("用户名长度不能少于 3 位");
        }

        // 2. 校验密码强度
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new BusinessException("密码长度不能少于 6 位");
        }

        // 3. 检查用户名是否存在
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, request.getUsername())
        );

        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }

        // 4. 创建用户（使用 Sa-Token 加密密码）
        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPassword(PasswordUtil.encrypt(request.getPassword(), request.getUsername()));
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setStatus(1);

        userMapper.insert(user);

        log.info("用户注册成功: userId={}, username={}", user.getId(), user.getUsername());
    }

    /**
     * 获取当前用户信息
     */
    public UserInfo getCurrentUserInfo() {
        Long userId = UserContext.getCurrentUserId();

        SysUser user = userMapper.selectById(userId);

        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        return buildUserInfo(user);
    }

    /**
     * 修改密码
     */
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        Long userId = UserContext.getCurrentUserId();

        SysUser user = userMapper.selectById(userId);

        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 验证旧密码
        if (!PasswordUtil.matches(request.getOldPassword(), user.getUsername(), user.getPassword())) {
            throw new BusinessException("原密码错误");
        }

        // 加密新密码
        user.setPassword(PasswordUtil.encrypt(request.getNewPassword(), user.getUsername()));
        userMapper.updateById(user);

        log.info("用户修改密码: userId={}", userId);

        // 修改密码后重新登录（可选）
        // StpUtil.logout();
    }

    /**
     * 构建用户信息 DTO
     */
    private UserInfo buildUserInfo(SysUser user) {
        return UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .email(user.getEmail())
                .phone(user.getPhone())
                .build();
    }
}
