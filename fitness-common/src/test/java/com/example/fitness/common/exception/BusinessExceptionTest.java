package com.example.fitness.common.exception;

import com.example.fitness.common.result.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * BusinessException 业务异常类单元测试
 */
@DisplayName("BusinessException 单元测试")
class BusinessExceptionTest {

    @Test
    @DisplayName("构造函数 (String) - 使用默认错误码500")
    void constructor_WithMessage_UsesDefaultCode() {
        BusinessException exception = new BusinessException("测试错误消息");

        assertEquals(500, exception.getCode());
        assertEquals("测试错误消息", exception.getMessage());
        assertEquals(ErrorCode.INTERNAL_SERVER_ERROR, exception.getErrorCode());
    }

    @Test
    @DisplayName("构造函数 (ErrorCode) - 使用枚举的码和消息")
    void constructor_WithErrorCode_UsesEnumValues() {
        BusinessException exception = new BusinessException(ErrorCode.PARAM_ERROR);

        assertEquals(400, exception.getCode());
        assertEquals("参数错误", exception.getMessage());
        assertEquals(ErrorCode.PARAM_ERROR, exception.getErrorCode());
    }

    @Test
    @DisplayName("构造函数 (Integer, String) - 自定义码和消息")
    void constructor_WithCodeAndMessage_UsesCustomValues() {
        BusinessException exception = new BusinessException(1001, "用户不存在");

        assertEquals(1001, exception.getCode());
        assertEquals("用户不存在", exception.getMessage());
        assertNull(exception.getErrorCode());
    }

    @Test
    @DisplayName("构造函数 (ErrorCode, String) - 使用枚举码但自定义消息")
    void constructor_WithErrorCodeAndMessage_UsesEnumCodeAndCustomMessage() {
        BusinessException exception = new BusinessException(ErrorCode.USER_NOT_FOUND, "找不到ID为123的用户");

        assertEquals(1001, exception.getCode());
        assertEquals("找不到ID为123的用户", exception.getMessage());
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("异常可以被捕获为 RuntimeException")
    void exception_CanBeCaughtAsRuntimeException() {
        try {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        } catch (RuntimeException e) {
            assertTrue(e instanceof BusinessException);
            assertEquals(401, ((BusinessException) e).getCode());
        }
    }

    @Test
    @DisplayName("各种 ErrorCode 构造验证")
    void constructor_AllErrorCodes_WorkCorrectly() {
        // 验证所有常用错误码都能正确构造
        assertDoesNotThrow(() -> new BusinessException(ErrorCode.SUCCESS));
        assertDoesNotThrow(() -> new BusinessException(ErrorCode.PARAM_ERROR));
        assertDoesNotThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
        assertDoesNotThrow(() -> new BusinessException(ErrorCode.FORBIDDEN));
        assertDoesNotThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        assertDoesNotThrow(() -> new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR));
        assertDoesNotThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    @Test
    @DisplayName("getter 方法验证")
    void getters_ReturnCorrectValues() {
        BusinessException exception = new BusinessException(ErrorCode.FORBIDDEN, "禁止访问此资源");

        // 验证 Lombok @Getter 生成的方法
        assertNotNull(exception.getCode());
        assertNotNull(exception.getErrorCode());
        assertNotNull(exception.getMessage());
    }
}
