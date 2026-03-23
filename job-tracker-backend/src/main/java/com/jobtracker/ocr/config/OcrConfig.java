package com.jobtracker.ocr.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

/**
 * OCR 配置类
 *
 * @author Job Tracker Team
 * @version 2.0.0
 * @since 2026-03-16
 */
@Configuration
@ConditionalOnProperty(name = "ocr.enabled", havingValue = "true", matchIfMissing = true)
public class OcrConfig {

    // ZhipuOcrClient 现在通过 @Component 注解自动注册为Bean
    // 不再需要手动创建Bean，因为官方SDK内部处理所有配置

}
