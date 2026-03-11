package com.jobtracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

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
public class JobTrackerApplication {

    /**
     * Application main entry point
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(JobTrackerApplication.class, args);
    }
}
