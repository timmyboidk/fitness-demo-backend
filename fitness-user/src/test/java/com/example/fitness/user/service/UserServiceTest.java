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

    @Test
    public void testOnboarding_CacheHit() throws Exception {
        // Arrange
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

        // Act
        userService.onboarding(request);

        // Assert
        // 1. Verify DB select was NOT called (Cache Hit)
        verify(userMapper, times(0)).selectById(userId);

        // 2. Verify Update was called
        verify(userMapper, times(1)).updateById(any(User.class));

        // 3. Verify Cache Invalidation (Delete) was called
        verify(redisTemplate, times(1)).delete("user:profile:" + userId);
    }

    @Test
    public void testOnboarding_CacheMiss() throws Exception {
        // Arrange
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

        // Act
        userService.onboarding(request);

        // Assert
        // 1. Verify DB select WAS called (Cache Miss)
        verify(userMapper, times(1)).selectById(userId);

        // 2. Verify Redis Set WAS called (Write Back)
        verify(valueOperations, times(1)).set(eq("user:profile:" + userId), anyString(), eq(3600L),
                eq(TimeUnit.SECONDS));

        // 3. Verify Update was called
        verify(userMapper, times(1)).updateById(any(User.class));

        // 4. Verify Cache Invalidation was called
        verify(redisTemplate, times(1)).delete("user:profile:" + userId);
    }
}
