package com.example.fitness.common.result;

import lombok.Data;

@Data
public class Result<T> {
    private boolean success;
    private String message;
    private T data;
    private Integer code;

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setSuccess(true);
        result.setData(data);
        result.setCode(200);
        return result;
    }

    public static <T> Result<T> error(String message) {
        return error(500, message);
    }

    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setSuccess(false);
        result.setMessage(message);
        result.setCode(code);
        return result;
    }
}
