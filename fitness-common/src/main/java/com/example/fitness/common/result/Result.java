package com.example.fitness.common.result;

import lombok.Data;

/**
 * 通用响应结果包装类
 */
@Data
public class Result<T> {
    private boolean success;
    private String message;
    private T data;
    private Integer code;

    /**
     * 成功响应
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setSuccess(true);
        result.setData(data);
        result.setCode(ErrorCode.SUCCESS.getCode());
        result.setMessage(ErrorCode.SUCCESS.getMessage());
        return result;
    }

    /**
     * 根据错误码枚举返回错误响应
     */
    public static <T> Result<T> error(ErrorCode errorCode) {
        return error(errorCode.getCode(), errorCode.getMessage());
    }

    /**
     * 错误响应 - 默认 500
     */
    public static <T> Result<T> error(String message) {
        return error(ErrorCode.INTERNAL_SERVER_ERROR.getCode(), message);
    }

    /**
     * 自定义错误码响应
     */
    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setSuccess(false);
        result.setMessage(message);
        result.setCode(code);
        return result;
    }
}
