package com.example.fitness.common.aspect;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.json.JSONUtil;
import com.example.fitness.common.annotation.Idempotent;
import com.example.fitness.common.exception.BusinessException;
import com.example.fitness.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * 幂等性切面处理
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class IdempotentAspect {

    private final StringRedisTemplate redisTemplate;

    @Before("@annotation(idempotent)")
    public void doBefore(JoinPoint point, Idempotent idempotent) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return;
        }
        HttpServletRequest request = attributes.getRequest();

        // 1. Generate unique key based on URL, IP, and Args
        String ip = request.getRemoteAddr();
        String url = request.getRequestURI();
        String args = JSONUtil.toJsonStr(point.getArgs());

        String source = ip + url + args;
        String key = idempotent.prefix() + SecureUtil.md5(source);

        // 2. Try to set key with expiration (SETNX)
        Boolean success = redisTemplate.opsForValue().setIfAbsent(key, "1", Duration.ofSeconds(idempotent.expire()));

        if (success == null || !success) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR.getCode(), idempotent.message());
        }
    }
}
