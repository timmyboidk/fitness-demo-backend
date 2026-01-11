package com.example.fitness.common.aspect;

import com.example.fitness.common.annotation.RateLimit;
import com.example.fitness.common.exception.BusinessException;
import com.example.fitness.common.result.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

/**
 * 限流切面处理
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private final StringRedisTemplate redisTemplate;

    /**
     * 限流核心逻辑：使用 Lua 脚本保证原子性
     */
    @Before("@annotation(rateLimit)")
    @SuppressWarnings("null")
    public void doBefore(JoinPoint point, RateLimit rateLimit) {
        int time = rateLimit.time();
        int count = rateLimit.count();

        // 1. 获取唯一的限流 Key
        String combineKey = getCombineKey(rateLimit, point);
        List<String> keys = Collections.singletonList(combineKey);

        // 2. Lua 脚本逻辑：检查累计访问次数。如果不存在则初始化并设置过期时间；如果未超限则递增。
        String luaScript = "if redis.call('get', KEYS[1]) == false then " +
                "redis.call('set', KEYS[1], 1) " +
                "redis.call('expire', KEYS[1], ARGV[1]) " +
                "return 1 " +
                "elseif tonumber(redis.call('get', KEYS[1])) < tonumber(ARGV[2]) then " +
                "redis.call('incr', KEYS[1]) " +
                "return 1 " +
                "else " +
                "return 0 " +
                "end";

        RedisScript<Long> redisScript = new DefaultRedisScript<>(luaScript, Long.class);
        Long number = redisTemplate.execute(redisScript, keys, String.valueOf(time), String.valueOf(count));

        if (number != null && number == 0) {
            log.warn("接口限流触发: key={}", combineKey);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR.getCode(), "访问过于频繁，请稍候再试");
        }
    }

    private String getCombineKey(RateLimit rateLimit, JoinPoint point) {
        StringBuilder stringBuffer = new StringBuilder(rateLimit.key());
        if (rateLimit.limitType() == RateLimit.LimitType.IP) {
            stringBuffer.append(getIpAddress()).append("-");
        }
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        Class<?> targetClass = method.getDeclaringClass();
        stringBuffer.append(targetClass.getName()).append("-").append(method.getName());
        return stringBuffer.toString();
    }

    private String getIpAddress() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return "unknown";
        }
        HttpServletRequest request = attributes.getRequest();
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
