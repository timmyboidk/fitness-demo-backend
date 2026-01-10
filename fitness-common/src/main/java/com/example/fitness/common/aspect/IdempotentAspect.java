package com.example.fitness.common.aspect;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.json.JSONUtil;
import com.example.fitness.common.annotation.Idempotent;
import com.example.fitness.common.exception.BusinessException;
import com.example.fitness.common.result.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;

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
    @SuppressWarnings("null")
    public void doBefore(JoinPoint point, Idempotent idempotent) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return;
        }
        HttpServletRequest request = attributes.getRequest();

        // 1. 根据 URL、IP 和方法参数生成唯一的 Key
        String ip = request.getRemoteAddr();
        String url = request.getRequestURI();
        String args = JSONUtil.toJsonStr(point.getArgs());

        String source = ip + url + args;
        String key = idempotent.prefix() + SecureUtil.md5(source);

        // 2. 尝试向 Redis 写入 Key，并设置过期时间 (SETNX)
        Boolean success = redisTemplate.opsForValue().setIfAbsent(key, "1", Duration.ofSeconds(idempotent.expire()));

        if (success == null || !success) {
            // 如果写入失败，说明请求已存在，抛出业务异常
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR.getCode(), idempotent.message());
        }
    }
}
