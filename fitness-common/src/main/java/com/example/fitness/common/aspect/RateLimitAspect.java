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
 * 接口限流切面处理器
 * 
 * <p>
 * 基于 Redis + Lua 脚本实现分布式限流功能，支持以下特性：
 * <ul>
 * <li>基于时间窗口的请求计数限流</li>
 * <li>支持 IP 级别和全局级别的限流策略</li>
 * <li>使用 Lua 脚本保证原子性操作，避免竞态条件</li>
 * </ul>
 * 
 * <p>
 * 使用方式：在需要限流的方法上添加 {@code @RateLimit} 注解，
 * 指定时间窗口（秒）和允许的最大请求次数。
 * 
 * @author fitness-team
 * @since 1.0.0
 * @see RateLimit
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    /** Redis 模板，用于执行限流脚本 */
    private final StringRedisTemplate redisTemplate;

    /**
     * 限流核心处理逻辑
     * 
     * <p>
     * 在目标方法执行前检查请求频率，若超过限制则抛出异常。
     * 使用 Redis Lua 脚本保证计数器操作的原子性：
     * <ol>
     * <li>若 key 不存在，初始化计数为 1 并设置过期时间</li>
     * <li>若计数未超限，原子递增计数器</li>
     * <li>若已超限，拒绝请求</li>
     * </ol>
     * 
     * @param point     切入点，包含被拦截方法的信息
     * @param rateLimit 限流注解，包含限流配置参数
     * @throws BusinessException 当请求频率超过限制时抛出
     */
    @Before("@annotation(rateLimit)")
    @SuppressWarnings("null")
    public void doBefore(JoinPoint point, RateLimit rateLimit) {
        int time = rateLimit.time();
        int count = rateLimit.count();

        // 1. 生成唯一的限流标识 Key
        String combineKey = getCombineKey(rateLimit, point);
        List<String> keys = Collections.singletonList(combineKey);

        // 2. Lua 脚本逻辑：检查累计访问次数
        // - 如果不存在则初始化并设置过期时间
        // - 如果未超限则递增
        // - 如果已超限则返回 0
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

        // 3. 若返回 0 表示已超限，拒绝请求
        if (number != null && number == 0) {
            log.warn("接口限流触发: key={}", combineKey);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR.getCode(), "访问过于频繁，请稍候再试");
        }
    }

    /**
     * 生成限流唯一标识 Key
     * 
     * <p>
     * Key 的组成格式：{注解key前缀}[-{客户端IP}]-{类名}-{方法名}
     * 
     * @param rateLimit 限流注解配置
     * @param point     切入点信息
     * @return 唯一的限流标识字符串
     */
    private String getCombineKey(RateLimit rateLimit, JoinPoint point) {
        StringBuilder stringBuffer = new StringBuilder(rateLimit.key());

        // 若限流类型为 IP 级别，则在 Key 中包含客户端 IP
        if (rateLimit.limitType() == RateLimit.LimitType.IP) {
            stringBuffer.append(getIpAddress()).append("-");
        }

        // 追加类名和方法名，确保唯一性
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        Class<?> targetClass = method.getDeclaringClass();
        stringBuffer.append(targetClass.getName()).append("-").append(method.getName());

        return stringBuffer.toString();
    }

    /**
     * 获取客户端真实 IP 地址
     * 
     * <p>
     * 优先从代理头中获取，支持以下 Header（按优先级）：
     * <ol>
     * <li>X-Forwarded-For（常见于 Nginx 反向代理）</li>
     * <li>Proxy-Client-IP（Apache 代理）</li>
     * <li>WL-Proxy-Client-IP（WebLogic 代理）</li>
     * <li>request.getRemoteAddr()（直连情况）</li>
     * </ol>
     * 
     * @return 客户端 IP 地址，若无法获取则返回 "unknown"
     */
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
