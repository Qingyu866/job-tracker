package com.jobtracker.common.exception;

/**
 * 业务异常
 * <p>
 * 用于处理业务逻辑中的异常情况
 * </p>
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 1.0.0
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
