package com.example.fitness.common.exception;

import com.example.fitness.common.result.ErrorCode;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final Integer code;
    private final ErrorCode errorCode;

    public BusinessException(String message) {
        super(message);
        this.code = ErrorCode.INTERNAL_SERVER_ERROR.getCode();
        this.errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
    }

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.errorCode = errorCode;
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.errorCode = null;
    }
}
