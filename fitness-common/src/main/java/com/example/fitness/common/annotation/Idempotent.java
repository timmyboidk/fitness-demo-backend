package com.example.fitness.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 幂等性控制注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Idempotent {

    /**
     * 幂等Key前缀
     */
    String prefix() default "idempotent:";

    /**
     * 锁过期时间（秒），默认5秒
     */
    int expire() default 5;

    /**
     * 提示信息
     */
    String message() default "请勿重复提交";
}
