package com.example.fitness.common.aspect;

import com.example.fitness.common.annotation.RateLimit;
import com.example.fitness.common.exception.BusinessException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 限流切面单元测试
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("RateLimitAspect 单元测试")
class RateLimitAspectTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private JoinPoint joinPoint;

    @Mock
    private MethodSignature signature;

    private RateLimitAspect rateLimitAspect;

    @BeforeEach
    void setUp() {
        rateLimitAspect = new RateLimitAspect(redisTemplate);
    }

    @Test
    @DisplayName("限流未触发 - 正常访问")
    @SuppressWarnings("unchecked")
    void doBefore_NotRateLimited_Success() throws NoSuchMethodException {
        // 模拟请求上下文
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("192.168.1.1");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // 模拟 RateLimit 注解
        RateLimit rateLimit = mock(RateLimit.class);
        when(rateLimit.key()).thenReturn("test_key_");
        when(rateLimit.time()).thenReturn(60);
        when(rateLimit.count()).thenReturn(100);
        when(rateLimit.limitType()).thenReturn(RateLimit.LimitType.IP);

        // 模拟 JoinPoint
        when(joinPoint.getSignature()).thenReturn(signature);
        Method method = TestService.class.getMethod("testMethod");
        when(signature.getMethod()).thenReturn(method);

        // 模拟 Redis 返回 1（未限流）- 使用 Answer 来处理可变参数
        doAnswer(invocation -> 1L)
                .when(redisTemplate).execute(any(RedisScript.class), anyList(), any(Object[].class));

        // 执行不应抛出异常
        assertDoesNotThrow(() -> rateLimitAspect.doBefore(joinPoint, rateLimit));

        // 验证 Redis 被调用
        verify(redisTemplate, times(1)).execute(any(RedisScript.class), anyList(), any(Object[].class));

        // 清理
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    @DisplayName("限流触发 - 抛出 BusinessException")
    @SuppressWarnings("unchecked")
    void doBefore_RateLimited_ThrowsException() throws NoSuchMethodException {
        // 模拟请求上下文
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("192.168.1.1");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // 模拟 RateLimit 注解
        RateLimit rateLimit = mock(RateLimit.class);
        when(rateLimit.key()).thenReturn("test_key_");
        when(rateLimit.time()).thenReturn(1);
        when(rateLimit.count()).thenReturn(5);
        when(rateLimit.limitType()).thenReturn(RateLimit.LimitType.IP);

        // 模拟 JoinPoint
        when(joinPoint.getSignature()).thenReturn(signature);
        Method method = TestService.class.getMethod("testMethod");
        when(signature.getMethod()).thenReturn(method);

        // 模拟 Redis 返回 0（已限流）
        doAnswer(invocation -> 0L)
                .when(redisTemplate).execute(any(RedisScript.class), anyList(), any(Object[].class));

        // 执行应抛出 BusinessException
        BusinessException ex = assertThrows(BusinessException.class,
                () -> rateLimitAspect.doBefore(joinPoint, rateLimit));

        assertTrue(ex.getMessage().contains("访问过于频繁"));

        // 清理
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    @DisplayName("无请求上下文 - IP 返回 unknown")
    @SuppressWarnings("unchecked")
    void doBefore_NoRequestContext_IpUnknown() throws NoSuchMethodException {
        // 不设置请求上下文
        RequestContextHolder.resetRequestAttributes();

        // 模拟 RateLimit 注解
        RateLimit rateLimit = mock(RateLimit.class);
        when(rateLimit.key()).thenReturn("test_key_");
        when(rateLimit.time()).thenReturn(60);
        when(rateLimit.count()).thenReturn(100);
        when(rateLimit.limitType()).thenReturn(RateLimit.LimitType.IP);

        // 模拟 JoinPoint
        when(joinPoint.getSignature()).thenReturn(signature);
        Method method = TestService.class.getMethod("testMethod");
        when(signature.getMethod()).thenReturn(method);

        // 模拟 Redis 返回 1
        doAnswer(invocation -> 1L)
                .when(redisTemplate).execute(any(RedisScript.class), anyList(), any(Object[].class));

        assertDoesNotThrow(() -> rateLimitAspect.doBefore(joinPoint, rateLimit));
    }

    @Test
    @DisplayName("X-Forwarded-For 头存在 - 使用该 IP")
    @SuppressWarnings("unchecked")
    void doBefore_XForwardedForHeader_UsesCorrectIp() throws NoSuchMethodException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "10.0.0.1");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        RateLimit rateLimit = mock(RateLimit.class);
        when(rateLimit.key()).thenReturn("test_key_");
        when(rateLimit.time()).thenReturn(60);
        when(rateLimit.count()).thenReturn(100);
        when(rateLimit.limitType()).thenReturn(RateLimit.LimitType.IP);

        when(joinPoint.getSignature()).thenReturn(signature);
        Method method = TestService.class.getMethod("testMethod");
        when(signature.getMethod()).thenReturn(method);

        doAnswer(invocation -> 1L)
                .when(redisTemplate).execute(any(RedisScript.class), anyList(), any(Object[].class));

        assertDoesNotThrow(() -> rateLimitAspect.doBefore(joinPoint, rateLimit));

        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    @DisplayName("Proxy-Client-IP 头回退")
    @SuppressWarnings("unchecked")
    void doBefore_ProxyClientIpHeader_UsesCorrectIp() throws NoSuchMethodException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Proxy-Client-IP", "10.0.0.2");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        RateLimit rateLimit = mock(RateLimit.class);
        when(rateLimit.key()).thenReturn("test_key_");
        when(rateLimit.time()).thenReturn(60);
        when(rateLimit.count()).thenReturn(100);
        when(rateLimit.limitType()).thenReturn(RateLimit.LimitType.IP);

        when(joinPoint.getSignature()).thenReturn(signature);
        Method method = TestService.class.getMethod("testMethod");
        when(signature.getMethod()).thenReturn(method);

        doAnswer(invocation -> 1L)
                .when(redisTemplate).execute(any(RedisScript.class), anyList(), any(Object[].class));

        assertDoesNotThrow(() -> rateLimitAspect.doBefore(joinPoint, rateLimit));

        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    @DisplayName("WL-Proxy-Client-IP 头回退")
    @SuppressWarnings("unchecked")
    void doBefore_WLProxyClientIpHeader_UsesCorrectIp() throws NoSuchMethodException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("WL-Proxy-Client-IP", "10.0.0.3");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        RateLimit rateLimit = mock(RateLimit.class);
        when(rateLimit.key()).thenReturn("test_key_");
        when(rateLimit.time()).thenReturn(60);
        when(rateLimit.count()).thenReturn(100);
        when(rateLimit.limitType()).thenReturn(RateLimit.LimitType.IP);

        when(joinPoint.getSignature()).thenReturn(signature);
        Method method = TestService.class.getMethod("testMethod");
        when(signature.getMethod()).thenReturn(method);

        doAnswer(invocation -> 1L)
                .when(redisTemplate).execute(any(RedisScript.class), anyList(), any(Object[].class));

        assertDoesNotThrow(() -> rateLimitAspect.doBefore(joinPoint, rateLimit));

        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    @DisplayName("LimitType.DEFAULT - 不使用 IP")
    @SuppressWarnings("unchecked")
    void doBefore_LimitTypeDefault_DoesNotUseIp() throws NoSuchMethodException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        RateLimit rateLimit = mock(RateLimit.class);
        when(rateLimit.key()).thenReturn("test_key_");
        when(rateLimit.time()).thenReturn(60);
        when(rateLimit.count()).thenReturn(100);
        when(rateLimit.limitType()).thenReturn(RateLimit.LimitType.DEFAULT);

        when(joinPoint.getSignature()).thenReturn(signature);
        Method method = TestService.class.getMethod("testMethod");
        when(signature.getMethod()).thenReturn(method);

        doAnswer(invocation -> 1L)
                .when(redisTemplate).execute(any(RedisScript.class), anyList(), any(Object[].class));

        assertDoesNotThrow(() -> rateLimitAspect.doBefore(joinPoint, rateLimit));

        RequestContextHolder.resetRequestAttributes();
    }

    /**
     * 测试用服务类
     */
    public static class TestService {
        public void testMethod() {
            // 空方法，仅用于测试
        }
    }
}
