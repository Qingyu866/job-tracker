package com.jobtracker.config;

import com.jobtracker.websocket.ChatWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket 配置类
 * <p>
 * 配置 WebSocket 端点和处理器
 * 支持 CORS 配置，允许前端跨域连接
 * </p>
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final ChatWebSocketHandler chatWebSocketHandler;

    /**
     * WebSocket 端点路径
     */
    public static final String WS_ENDPOINT = "/ws/chat";

    /**
     * 允许的跨域来源（前端开发服务器）
     */
    private static final String ALLOWED_ORIGINS = "http://localhost:5173";

    /**
     * 注册 WebSocket 处理器
     *
     * @param registry WebSocket 处理器注册表
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatWebSocketHandler, WS_ENDPOINT)
                .setAllowedOrigins(ALLOWED_ORIGINS)
                .withSockJS(); // 启用 SockJS 支持（降级方案）
    }
}
