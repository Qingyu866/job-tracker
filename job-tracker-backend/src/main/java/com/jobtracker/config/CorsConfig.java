package com.jobtracker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * CORS 跨域配置类
 * <p>
 * 配置允许前端跨域访问后端 API
 * 支持开发环境和生产环境的不同配置
 * </p>
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Configuration
public class CorsConfig {

    /**
     * 允许的跨域来源
     * <p>
     * 开发环境：http://localhost:5173
     * 生产环境：根据实际部署域名添加
     * </p>
     */
    private static final String[] ALLOWED_ORIGINS = {
            "http://localhost:5173",
            "http://localhost:3000",
            "http://127.0.0.1:5173",
            "http://127.0.0.1:3000"
    };

    /**
     * 创建 CORS 过滤器
     * <p>
     * 配置跨域请求的详细规则：
     * - 允许的域名
     * - 允许的 HTTP 方法
     * - 允许的请求头
     * - 是否允许发送凭证
     * - 预检请求的缓存时间
     * </p>
     *
     * @return CORS 过滤器 Bean
     */
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // 允许凭证（Cookie 等）
        config.setAllowCredentials(true);

        // 允许的域名
        config.addAllowedOriginPattern("*");

        // 允许的请求头
        config.addAllowedHeader("*");

        // 允许的 HTTP 方法
        config.addAllowedMethod("*");

        // 预检请求的有效期（秒）
        config.setMaxAge(3600L);

        // 对所有路径应用此配置
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}
