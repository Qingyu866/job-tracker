package com.jobtracker.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import com.jobtracker.interceptor.LoginInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 配置类
 * <p>
 * 配置 Sa-Token 拦截器和登录校验规则
 * </p>
 * <p>
 * 注意：Sa-Token Redis 存储由 sa-token-redis-jackson 自动配置，无需手动创建 SaTokenDao bean
 * </p>
 *
 * @author Job Tracker Team
 * @since 2026-03-17
 */
@Configuration
@RequiredArgsConstructor
public class SaTokenConfig implements WebMvcConfigurer {

    private final LoginInterceptor loginInterceptor;

    /**
     * 注册 Sa-Token 拦截器
     */
    @Bean
    public SaInterceptor getSaInterceptor() {
        return new SaInterceptor(handle -> {
            // 登录校验 - 白名单路径除外
            // 注意：SaRouter 在拦截器中看到的路径不包含 context-path
            SaRouter
                    .match("/**")
                    .notMatch("/auth/login")
                    .notMatch("/auth/register")
                    .notMatch("/ws/chat")
                    .notMatch("/health")
                    .check(r -> StpUtil.checkLogin());

            // 权限认证：管理员接口
            SaRouter
                    .match("/admin/**")
                    .check(r -> StpUtil.checkRole("ADMIN"));
        });

    }

    /**
     * 注册拦截器到 MVC
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Sa-Token 拦截器（处理登录校验）
        registry.addInterceptor(getSaInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/error", "/favicon.ico");

        // 登录拦截器（设置用户上下文）
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/error", "/favicon.ico");
    }
}
