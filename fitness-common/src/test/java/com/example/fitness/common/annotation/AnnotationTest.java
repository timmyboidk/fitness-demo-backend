package com.example.fitness.common.annotation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 自定义注解单元测试
 * 
 * <p>
 * 验证注解的默认值和属性配置是否正确
 * </p>
 */
@DisplayName("自定义注解单元测试")
class AnnotationTest {

    @Nested
    @DisplayName("Idempotent 注解测试")
    class IdempotentAnnotationTest {

        @Test
        @DisplayName("Idempotent - 默认值验证")
        void idempotent_DefaultValues() throws NoSuchMethodException {
            Method method = TestClass.class.getMethod("defaultIdempotentMethod");
            Idempotent annotation = method.getAnnotation(Idempotent.class);

            assertNotNull(annotation);
            assertEquals("idempotent:", annotation.prefix());
            assertEquals(5, annotation.expire());
            assertEquals("请勿重复提交", annotation.message());
        }

        @Test
        @DisplayName("Idempotent - 自定义值验证")
        void idempotent_CustomValues() throws NoSuchMethodException {
            Method method = TestClass.class.getMethod("customIdempotentMethod");
            Idempotent annotation = method.getAnnotation(Idempotent.class);

            assertNotNull(annotation);
            assertEquals("custom:", annotation.prefix());
            assertEquals(10, annotation.expire());
            assertEquals("操作频繁，请稍后再试", annotation.message());
        }
    }

    @Nested
    @DisplayName("RateLimit 注解测试")
    class RateLimitAnnotationTest {

        @Test
        @DisplayName("RateLimit - 默认值验证")
        void rateLimit_DefaultValues() throws NoSuchMethodException {
            Method method = TestClass.class.getMethod("defaultRateLimitMethod");
            RateLimit annotation = method.getAnnotation(RateLimit.class);

            assertNotNull(annotation);
            assertEquals("", annotation.key());
            assertEquals(60, annotation.time());
            assertEquals(100, annotation.count());
            assertEquals(RateLimit.LimitType.DEFAULT, annotation.limitType());
        }

        @Test
        @DisplayName("RateLimit - IP限流类型验证")
        void rateLimit_IpLimitType() throws NoSuchMethodException {
            Method method = TestClass.class.getMethod("ipRateLimitMethod");
            RateLimit annotation = method.getAnnotation(RateLimit.class);

            assertNotNull(annotation);
            assertEquals("login:", annotation.key());
            assertEquals(60, annotation.time());
            assertEquals(5, annotation.count());
            assertEquals(RateLimit.LimitType.IP, annotation.limitType());
        }

        @Test
        @DisplayName("RateLimit - CUSTOMER限流类型验证")
        void rateLimit_CustomerLimitType() throws NoSuchMethodException {
            Method method = TestClass.class.getMethod("customerRateLimitMethod");
            RateLimit annotation = method.getAnnotation(RateLimit.class);

            assertNotNull(annotation);
            assertEquals(RateLimit.LimitType.CUSTOMER, annotation.limitType());
        }

        @Test
        @DisplayName("LimitType - 枚举值验证")
        void limitType_AllValues() {
            RateLimit.LimitType[] values = RateLimit.LimitType.values();

            assertEquals(3, values.length);
            assertNotNull(RateLimit.LimitType.valueOf("DEFAULT"));
            assertNotNull(RateLimit.LimitType.valueOf("CUSTOMER"));
            assertNotNull(RateLimit.LimitType.valueOf("IP"));
        }
    }

    /**
     * 用于测试注解的测试类
     */
    public static class TestClass {

        @Idempotent
        public void defaultIdempotentMethod() {
        }

        @Idempotent(prefix = "custom:", expire = 10, message = "操作频繁，请稍后再试")
        public void customIdempotentMethod() {
        }

        @RateLimit
        public void defaultRateLimitMethod() {
        }

        @RateLimit(key = "login:", time = 60, count = 5, limitType = RateLimit.LimitType.IP)
        public void ipRateLimitMethod() {
        }

        @RateLimit(limitType = RateLimit.LimitType.CUSTOMER)
        public void customerRateLimitMethod() {
        }
    }
}
