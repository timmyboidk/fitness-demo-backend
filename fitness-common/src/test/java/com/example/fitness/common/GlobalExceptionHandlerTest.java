package com.example.fitness.common;

import com.example.fitness.common.exception.BusinessException;
import com.example.fitness.common.exception.GlobalExceptionHandler;
import com.example.fitness.common.result.Result;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    public void testHandleException() {
        Exception e = new RuntimeException("runtime error");
        Result<?> result = handler.handleException(e);
        Assertions.assertFalse(result.isSuccess());
        Assertions.assertEquals(500, result.getCode());
        Assertions.assertEquals("runtime error", result.getMessage());
    }

    @Test
    public void testHandleBusinessException() {
        BusinessException e = new BusinessException(404, "not found");
        Result<?> result = handler.handleBusinessException(e);
        Assertions.assertFalse(result.isSuccess());
        Assertions.assertEquals(404, result.getCode());
        Assertions.assertEquals("not found", result.getMessage());
    }
}
