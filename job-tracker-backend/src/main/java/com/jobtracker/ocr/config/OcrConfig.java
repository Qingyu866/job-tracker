package com.jobtracker.ocr.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.jobtracker.ocr.ZhipuOcrClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OCR 配置类
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 2026-03-16
 */
@Configuration
@ConditionalOnProperty(name = "ocr.enabled", havingValue = "true", matchIfMissing = true)
public class OcrConfig {

    /**
     * 配置专用于 OCR 的 ObjectMapper
     * 使用蛇形命名策略匹配智谱 API
     */
    @Bean("ocrObjectMapper")
    public ObjectMapper ocrObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // 使用蛇形命名策略
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        return mapper;
    }

    /**
     * 智谱 OCR 客户端
     */
    @Bean
    public ZhipuOcrClient zhipuOcrClient(ObjectMapper ocrObjectMapper) {
        return new ZhipuOcrClient(ocrObjectMapper);
    }
}
