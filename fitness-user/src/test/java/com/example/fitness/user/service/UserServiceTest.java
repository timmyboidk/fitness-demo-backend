package com.example.fitness.user.service;

import com.example.fitness.user.mapper.UserMapper;
import com.example.fitness.user.model.entity.User;
import com.example.fitness.user.service.impl.UserServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 用户服务单元测试
 * 验证首次使用落地流程中的缓存管理逻辑（Cache-Aside 模式）。
 */
@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private UserServiceImpl userService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 测试首次使用落地：缓存命中场景
     */
    @Test
    public void testOnboarding_CacheHit() throws Exception {
        // 准备数据
        String userId = "1";
        User user = new User();
        user.setId(1L);
        user.setDifficultyLevel("novice");
        String userJson = objectMapper.writeValueAsString(user);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("user:profile:" + userId)).thenReturn(userJson);

        Map<String, Object> request = new HashMap<>();
        request.put("userId", userId);
        request.put("difficultyLevel", "expert");

        // 执行动作
        userService.onboarding(request);

        // 验证断言
        // 1. 验证数据库查询未被调用（缓存命中）
        verify(userMapper, times(0)).selectById(userId);

        // 2. 验证更新方法被调用
        verify(userMapper, times(1)).updateById(any(User.class));

        // 3. 验证缓存失效（删除）被调用，保证一致性
        verify(redisTemplate, times(1)).delete("user:profile:" + userId);
    }

    /**
     * 测试首次使用落地：缓存未命中场景
     */
    @Test
    public void testOnboarding_CacheMiss() throws Exception {
        // 准备数据
        String userId = "2";
        User user = new User();
        user.setId(2L);
        user.setDifficultyLevel("novice");

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("user:profile:" + userId)).thenReturn(null);
        when(userMapper.selectById(userId)).thenReturn(user);

        Map<String, Object> request = new HashMap<>();
        request.put("userId", userId);
        request.put("difficultyLevel", "intermediate");

        // 执行动作
        userService.onboarding(request);

        // 验证断言
        // 1. 验证数据库查询被调用（缓存未命中）
        verify(userMapper, times(1)).selectById(userId);

        // 2. 验证回写 Redis 被调用
        verify(valueOperations, times(1)).set(eq("user:profile:" + userId), anyString(), eq(3600L),
                eq(TimeUnit.SECONDS));

        // 3. 验证更新方法被调用
        verify(userMapper, times(1)).updateById(any(User.class));

        // 4. 验证缓存失效（删除）被调用
        verify(redisTemplate, times(1)).delete("user:profile:" + userId);
    }
}
