package com.jobtracker.auth.context;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Set;

/**
 * 用户信息持有者
 * <p>
 * 存储当前登录用户的基本信息
 * 通过 UserContext 在 ThreadLocal 中共享
 * </p>
 *
 * @author Job Tracker Team
 * @since 2026-03-17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoHolder implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 角色列表
     */
    @Builder.Default
    private Set<String> roles = Set.of();

    /**
     * 权限列表
     */
    @Builder.Default
    private Set<String> permissions = Set.of();
}
