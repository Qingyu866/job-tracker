package com.jobtracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应 DTO
 *
 * @author Job Tracker Team
 * @since 2026-03-17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    /**
     * Token 值
     */
    private String token;

    /**
     * Token 名称
     */
    private String tokenName;

    /**
     * 用户信息
     */
    private UserInfo userInfo;
}
