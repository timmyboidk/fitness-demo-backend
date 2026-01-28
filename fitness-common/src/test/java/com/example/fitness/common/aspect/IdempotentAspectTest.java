package com.example.fitness.common.aspect;

import com.example.fitness.common.annotation.Idempotent;
import com.example.fitness.common.exception.BusinessException;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * 幂等性切面单元测试
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("IdempotentAspect 单元测试")
class IdempotentAspectTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private JoinPoint joinPoint;

    private IdempotentAspect idempotentAspect;

    @BeforeEach
    void setUp() {
        idempotentAspect = new IdempotentAspect(redisTemplate);
    }

    @Test
    @DisplayName("首次请求 - 幂等校验通过")
    void doBefore_FirstRequest_Success() {
        // 模拟请求上下文
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("192.168.1.1");
        request.setRequestURI("/api/test");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // 模拟 Idempotent 注解
        Idempotent idempotent = mock(Idempotent.class);
        when(idempotent.prefix()).thenReturn("idempotent:");
        when(idempotent.expire()).thenReturn(5);
        when(idempotent.message()).thenReturn("请勿重复提交");

        // 模拟 JoinPoint
        when(joinPoint.getArgs()).thenReturn(new Object[] { "arg1", 123 });

        // 模拟 Redis 写入成功
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class)))
                .thenReturn(true);

        // 执行不应抛出异常
        assertDoesNotThrow(() -> idempotentAspect.doBefore(joinPoint, idempotent));

        // 验证 Redis 被调用
        verify(valueOperations).setIfAbsent(anyString(), eq("1"), any(Duration.class));

        // 清理
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    @DisplayName("重复请求 - 幂等校验失败抛出异常")
    void doBefore_DuplicateRequest_ThrowsException() {
        // 模拟请求上下文
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("192.168.1.1");
        request.setRequestURI("/api/test");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // 模拟 Idempotent 注解
        Idempotent idempotent = mock(Idempotent.class);
        when(idempotent.prefix()).thenReturn("idempotent:");
        when(idempotent.expire()).thenReturn(5);
        when(idempotent.message()).thenReturn("请勿重复提交");

        // 模拟 JoinPoint
        when(joinPoint.getArgs()).thenReturn(new Object[] { "arg1" });

        // 模拟 Redis 写入失败（Key 已存在）
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class)))
                .thenReturn(false);

        // 执行应抛出 BusinessException
        BusinessException ex = assertThrows(BusinessException.class,
                () -> idempotentAspect.doBefore(joinPoint, idempotent));

        assertEquals("请勿重复提交", ex.getMessage());

        // 清理
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    @DisplayName("Redis 返回 null - 幂等校验失败")
    void doBefore_RedisReturnsNull_ThrowsException() {
        // 模拟请求上下文
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("192.168.1.1");
        request.setRequestURI("/api/test");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // 模拟 Idempotent 注解
        Idempotent idempotent = mock(Idempotent.class);
        when(idempotent.prefix()).thenReturn("idempotent:");
        when(idempotent.expire()).thenReturn(5);
        when(idempotent.message()).thenReturn("操作过于频繁");

        // 模拟 JoinPoint
        when(joinPoint.getArgs()).thenReturn(new Object[] {});

        // 模拟 Redis 返回 null
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class)))
                .thenReturn(null);

        // 执行应抛出 BusinessException
        BusinessException ex = assertThrows(BusinessException.class,
                () -> idempotentAspect.doBefore(joinPoint, idempotent));

        assertEquals("操作过于频繁", ex.getMessage());

        // 清理
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    @DisplayName("无请求上下文 - 直接返回不处理")
    void doBefore_NoRequestContext_ReturnsEarly() {
        // 不设置请求上下文
        RequestContextHolder.resetRequestAttributes();

        // 模拟 Idempotent 注解
        Idempotent idempotent = mock(Idempotent.class);

        // 执行不应抛出异常，也不应调用 Redis
        assertDoesNotThrow(() -> idempotentAspect.doBefore(joinPoint, idempotent));

        // 验证 Redis 未被调用
        verify(redisTemplate, never()).opsForValue();
    }

    @Test
    @DisplayName("不同过期时间 - 正确传递")
    void doBefore_DifferentExpireTime_PassesToRedis() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.1");
        request.setRequestURI("/api/submit");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        Idempotent idempotent = mock(Idempotent.class);
        when(idempotent.prefix()).thenReturn("submit:");
        when(idempotent.expire()).thenReturn(30); // 30秒过期
        when(idempotent.message()).thenReturn("提交中，请稍候");

        when(joinPoint.getArgs()).thenReturn(new Object[] { "data" });

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class)))
                .thenReturn(true);

        assertDoesNotThrow(() -> idempotentAspect.doBefore(joinPoint, idempotent));

        // 验证过期时间正确
        verify(valueOperations).setIfAbsent(anyString(), eq("1"), eq(Duration.ofSeconds(30)));

        RequestContextHolder.resetRequestAttributes();
    }
}
