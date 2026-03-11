package com.jobtracker.common.exception;

import com.jobtracker.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * <p>
 * 统一处理应用中抛出的各类异常，返回标准化的错误响应
 * </p>
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     *
     * @param ex 业务异常
     * @return 错误响应
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBusinessException(BusinessException ex) {
        log.warn("业务异常：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }

    /**
     * 处理资源不存在异常
     *
     * @param ex 资源不存在异常
     * @return 错误响应
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<Void> handleResourceNotFoundException(ResourceNotFoundException ex) {
        log.warn("资源不存在：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }

    /**
     * 处理参数校验异常（@Valid）
     *
     * @param ex 参数校验异常
     * @return 错误响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleValidationException(MethodArgumentNotValidException ex) {
        String errorMsg = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数校验失败：{}", errorMsg);
        return Result.error("参数校验失败：" + errorMsg);
    }

    /**
     * 处理参数绑定异常
     *
     * @param ex 参数绑定异常
     * @return 错误响应
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBindException(BindException ex) {
        String errorMsg = ex.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数绑定失败：{}", errorMsg);
        return Result.error("参数绑定失败：" + errorMsg);
    }

    /**
     * 处理参数类型不匹配异常
     *
     * @param ex 参数类型不匹配异常
     * @return 错误响应
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        String errorMsg = String.format("参数 '%s' 类型不匹配，期望类型：%s",
                ex.getName(), ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "未知");
        log.warn("参数类型不匹配：{}", errorMsg);
        return Result.error(errorMsg);
    }

    /**
     * 处理非法参数异常
     *
     * @param ex 非法参数异常
     * @return 错误响应
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("非法参数：{}", ex.getMessage());
        return Result.error("参数错误：" + ex.getMessage());
    }

    /**
     * 处理非法状态异常
     *
     * @param ex 非法状态异常
     * @return 错误响应
     */
    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Result<Void> handleIllegalStateException(IllegalStateException ex) {
        log.warn("非法状态：{}", ex.getMessage());
        return Result.error("状态错误：" + ex.getMessage());
    }

    /**
     * 处理数据库异常
     *
     * @param ex 数据库异常
     * @return 错误响应
     */
    @ExceptionHandler(org.springframework.dao.DataAccessException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleDataAccessException(org.springframework.dao.DataAccessException ex) {
        log.error("数据库异常", ex);
        return Result.error("数据库操作失败，请稍后重试");
    }

    /**
     * 处理 AI 服务异常
     *
     * @param ex AI 服务异常
     * @return 错误响应
     */
    @ExceptionHandler(AIServiceException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Result<Void> handleAIServiceException(AIServiceException ex) {
        log.error("AI 服务异常：{}", ex.getMessage(), ex);
        return Result.error("AI 服务暂时不可用：" + ex.getMessage());
    }

    /**
     * 处理未知异常
     *
     * @param ex 异常
     * @return 错误响应
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception ex) {
        log.error("系统异常", ex);
        return Result.error("系统异常，请联系管理员");
    }
}
