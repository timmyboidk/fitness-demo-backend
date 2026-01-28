package com.example.fitness.common.result;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Result 响应类单元测试
 */
@DisplayName("Result 响应类单元测试")
class ResultTest {

    @Test
    @DisplayName("success - 创建成功响应携带数据")
    void success_WithData_ReturnsSuccessResult() {
        String testData = "Test Data";

        Result<String> result = Result.success(testData);

        assertTrue(result.isSuccess());
        assertEquals("Test Data", result.getData());
        assertEquals(200, result.getCode());
        assertEquals("操作成功", result.getMessage());
    }

    @Test
    @DisplayName("success - null 数据也能正常创建")
    void success_WithNullData_ReturnsSuccessResult() {
        Result<Object> result = Result.success(null);

        assertTrue(result.isSuccess());
        assertNull(result.getData());
        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("error(ErrorCode) - 根据错误码枚举创建错误响应")
    void error_WithErrorCode_ReturnsErrorResult() {
        Result<Object> result = Result.error(ErrorCode.PARAM_ERROR);

        assertFalse(result.isSuccess());
        assertEquals(400, result.getCode());
        assertEquals("参数错误", result.getMessage());
        assertNull(result.getData());
    }

    @Test
    @DisplayName("error(String) - 使用默认错误码创建错误响应")
    void error_WithMessage_ReturnsErrorWithDefaultCode() {
        Result<Object> result = Result.error("自定义错误消息");

        assertFalse(result.isSuccess());
        assertEquals(500, result.getCode());
        assertEquals("自定义错误消息", result.getMessage());
    }

    @Test
    @DisplayName("error(Integer, String) - 自定义错误码和消息")
    void error_WithCodeAndMessage_ReturnsCustomError() {
        Result<Object> result = Result.error(403, "禁止访问");

        assertFalse(result.isSuccess());
        assertEquals(403, result.getCode());
        assertEquals("禁止访问", result.getMessage());
    }

    @Test
    @DisplayName("getters/setters - 验证所有字段")
    void gettersSetters_AllFields_WorkCorrectly() {
        Result<Integer> result = new Result<>();

        result.setSuccess(true);
        result.setCode(201);
        result.setMessage("创建成功");
        result.setData(42);

        assertTrue(result.isSuccess());
        assertEquals(201, result.getCode());
        assertEquals("创建成功", result.getMessage());
        assertEquals(42, result.getData());
    }

    @Test
    @DisplayName("error(ErrorCode.USER_NOT_FOUND) - 用户不存在错误")
    void error_UserNotFound_ReturnsCorrectError() {
        Result<Object> result = Result.error(ErrorCode.USER_NOT_FOUND);

        assertFalse(result.isSuccess());
        assertEquals(1001, result.getCode());
        assertEquals("用户不存在", result.getMessage());
    }

    @Test
    @DisplayName("error(ErrorCode.UNAUTHORIZED) - 未授权错误")
    void error_Unauthorized_ReturnsCorrectError() {
        Result<Object> result = Result.error(ErrorCode.UNAUTHORIZED);

        assertFalse(result.isSuccess());
        assertEquals(401, result.getCode());
        assertEquals("未授权", result.getMessage());
    }
}
