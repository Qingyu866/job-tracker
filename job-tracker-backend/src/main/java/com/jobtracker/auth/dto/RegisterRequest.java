package com.jobtracker.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 注册请求 DTO
 *
 * @author Job Tracker Team
 * @since 2026-03-17
 */
@Data
public class RegisterRequest {

    /**
     * 用户名（3-20位）
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度为3-20位")
    private String username;

    /**
     * 密码（6-20位）
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度为6-20位")
    private String password;

    /**
     * 昵称
     */
    @Size(max = 50, message = "昵称最多50位")
    private String nickname;

    /**
     * 邮箱
     */
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 手机号
     */
    @Size(max = 20, message = "手机号最多20位")
    private String phone;
}
