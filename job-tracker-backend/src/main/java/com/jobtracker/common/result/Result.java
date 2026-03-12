package com.jobtracker.common.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一响应结果封装
 * <p>
 * 封装所有 API 接口的返回结果，包括：
 * - 响应码（成功/失败）
 * - 响应消息
 * - 响应数据
 * </p>
 *
 * @author Job Tracker Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {

    /**
     * 响应码（0 表示成功，其他表示失败）
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 时间戳
     */
    private Long timestamp;

    /**
     * 成功响应（无数据）
     *
     * @param <T> 数据类型
     * @return Result 实例
     */
    public static <T> Result<T> success() {
        return Result.<T>builder()
                .code(0)
                .message("操作成功")
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 成功响应（带数据）
     *
     * @param data 响应数据
     * @param <T>  数据类型
     * @return Result 实例
     */
    public static <T> Result<T> success(T data) {
        return Result.<T>builder()
                .code(0)
                .message("操作成功")
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 成功响应（带消息和数据）
     *
     * @param message 响应消息
     * @param data    响应数据
     * @param <T>     数据类型
     * @return Result 实例
     */
    public static <T> Result<T> success(String message, T data) {
        return Result.<T>builder()
                .code(0)
                .message(message)
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 失败响应（默认错误消息）
     *
     * @param <T> 数据类型
     * @return Result 实例
     */
    public static <T> Result<T> error() {
        return Result.<T>builder()
                .code(-1)
                .message("操作失败")
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 失败响应（自定义消息）
     *
     * @param message 错误消息
     * @param <T>     数据类型
     * @return Result 实例
     */
    public static <T> Result<T> error(String message) {
        return Result.<T>builder()
                .code(-1)
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 失败响应（自定义错误码和消息）
     *
     * @param code    错误码
     * @param message 错误消息
     * @param <T>     数据类型
     * @return Result 实例
     */
    public static <T> Result<T> error(Integer code, String message) {
        return Result.<T>builder()
                .code(code)
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 判断响应是否成功
     *
     * @return 如果成功返回 true，否则返回 false
     */
    public boolean isSuccess() {
        return this.code != null && this.code == 0;
    }
}
