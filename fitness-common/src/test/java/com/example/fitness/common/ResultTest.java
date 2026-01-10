package com.example.fitness.common;

import com.example.fitness.common.result.Result;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ResultTest {

    @Test
    public void testSuccess() {
        Result<String> result = Result.success("ok");
        Assertions.assertTrue(result.isSuccess());
        Assertions.assertEquals(200, result.getCode());
        Assertions.assertEquals("ok", result.getData());
    }

    @Test
    public void testError() {
        Result<Void> result = Result.error("fail");
        Assertions.assertFalse(result.isSuccess());
        Assertions.assertEquals(500, result.getCode());
        Assertions.assertEquals("fail", result.getMessage());
    }

    @Test
    public void testErrorCode() {
        Result<Void> result = Result.error(400, "bad request");
        Assertions.assertFalse(result.isSuccess());
        Assertions.assertEquals(400, result.getCode());
        Assertions.assertEquals("bad request", result.getMessage());
    }
}
