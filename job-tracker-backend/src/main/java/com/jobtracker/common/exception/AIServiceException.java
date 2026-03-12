package com.jobtracker.common.exception;

/**
 * AI 服务异常
 * <p>
 * 用于处理 AI 服务调用失败的情况
 * </p>
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 1.0.0
 */
public class AIServiceException extends RuntimeException {

    public AIServiceException(String message) {
        super(message);
    }

    public AIServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
