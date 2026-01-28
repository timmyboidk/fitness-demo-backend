package com.example.fitness.common;

import com.example.fitness.common.exception.BusinessException;
import com.example.fitness.common.exception.GlobalExceptionHandler;
import com.example.fitness.common.result.Result;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 全局异常处理器单元测试
 * 
 * <p>
 * 覆盖所有异常处理方法:
 * </p>
 * <ul>
 * <li>普通异常 (Exception)</li>
 * <li>业务异常 (BusinessException)</li>
 * <li>参数校验异常 (MethodArgumentNotValidException)</li>
 * </ul>
 */
@DisplayName("GlobalExceptionHandler 单元测试")
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    /**
     * 测试普通运行时异常的处理
     */
    @Test
    @DisplayName("handleException - 处理普通运行时异常")
    void handleException_RuntimeException_ReturnsErrorResult() {
        Exception e = new RuntimeException("runtime error");
        Result<?> result = handler.handleException(e);

        assertFalse(result.isSuccess());
        assertEquals(500, result.getCode());
        assertEquals("runtime error", result.getMessage());
    }

    @Test
    @DisplayName("handleException - 处理空消息异常")
    void handleException_NullMessage_ReturnsNullMessage() {
        Exception e = new RuntimeException((String) null);
        Result<?> result = handler.handleException(e);

        assertFalse(result.isSuccess());
        assertEquals(500, result.getCode());
        assertNull(result.getMessage());
    }

    @Test
    @DisplayName("handleBusinessException - 处理业务异常")
    void handleBusinessException_ReturnsCorrectCodeAndMessage() {
        BusinessException e = new BusinessException(404, "not found");
        Result<?> result = handler.handleBusinessException(e);

        assertFalse(result.isSuccess());
        assertEquals(404, result.getCode());
        assertEquals("not found", result.getMessage());
    }

    @Test
    @DisplayName("handleBusinessException - 处理自定义错误码业务异常")
    void handleBusinessException_CustomCode_ReturnsCustomCode() {
        BusinessException e = new BusinessException(1001, "用户不存在");
        Result<?> result = handler.handleBusinessException(e);

        assertFalse(result.isSuccess());
        assertEquals(1001, result.getCode());
        assertEquals("用户不存在", result.getMessage());
    }

    @Test
    @DisplayName("handleValidationException - 处理参数校验异常带字段错误")
    void handleValidationException_WithFieldError_ReturnsFieldMessage() throws NoSuchMethodException {
        // 创建一个模拟的绑定结果
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "testObject");
        bindingResult.addError(new FieldError(
                "testObject",
                "username",
                "用户名不能为空"));

        MethodParameter methodParameter = new MethodParameter(
                this.getClass().getDeclaredMethod("handleValidationException_WithFieldError_ReturnsFieldMessage"),
                -1);

        MethodArgumentNotValidException e = new MethodArgumentNotValidException(
                methodParameter,
                bindingResult);

        Result<?> result = handler.handleValidationException(e);

        assertFalse(result.isSuccess());
        assertEquals(400, result.getCode());
        assertEquals("用户名不能为空", result.getMessage());
    }

    @Test
    @DisplayName("handleValidationException - 处理参数校验异常无字段错误")
    void handleValidationException_WithoutFieldError_ReturnsDefaultMessage() throws NoSuchMethodException {
        // 创建一个没有字段错误的绑定结果
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "testObject");

        MethodParameter methodParameter = new MethodParameter(
                this.getClass().getDeclaredMethod("handleValidationException_WithoutFieldError_ReturnsDefaultMessage"),
                -1);

        MethodArgumentNotValidException e = new MethodArgumentNotValidException(
                methodParameter,
                bindingResult);

        Result<?> result = handler.handleValidationException(e);

        assertFalse(result.isSuccess());
        assertEquals(400, result.getCode());
        assertEquals("Request validation failed", result.getMessage());
    }

    @Test
    @DisplayName("handleValidationException - 多个字段错误返回第一个")
    void handleValidationException_MultipleFieldErrors_ReturnsFirst() throws NoSuchMethodException {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "testObject");
        bindingResult.addError(new FieldError("testObject", "email", "邮箱格式不正确"));
        bindingResult.addError(new FieldError("testObject", "phone", "手机号格式不正确"));

        MethodParameter methodParameter = new MethodParameter(
                this.getClass().getDeclaredMethod("handleValidationException_MultipleFieldErrors_ReturnsFirst"),
                -1);

        MethodArgumentNotValidException e = new MethodArgumentNotValidException(
                methodParameter,
                bindingResult);

        Result<?> result = handler.handleValidationException(e);

        assertFalse(result.isSuccess());
        assertEquals(400, result.getCode());
        // 返回第一个字段错误
        assertEquals("邮箱格式不正确", result.getMessage());
    }
}
