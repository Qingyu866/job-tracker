package com.jobtracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Job Tracker Backend Application
 *
 * Main application entry point for the Job Application Tracking System.
 * Integrates Spring Boot 3.2+ with LangChain4j, MyBatis Plus, and WebSocket support.
 *
 * @author Job Tracker Team
 * @version 1.0.0
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.jobtracker")
@EnableScheduling  // 启用定时任务，用于将来的记忆清理等功能
public class JobTrackerApplication {

    /**
     * Application main entry point
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        // 强制 Java HttpClient 使用 HTTP/1.1（兼容 LM Studio）
        System.setProperty("jdk.httpclient.HttpClient.log", "errors,requests,headers");
        System.setProperty("jdk.httpclient.allowRestrictedHeaders", "Connection,Close");

        SpringApplication.run(JobTrackerApplication.class, args);
    }
}
