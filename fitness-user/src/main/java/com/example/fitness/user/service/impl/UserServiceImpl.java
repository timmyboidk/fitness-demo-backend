package com.example.fitness.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import java.util.concurrent.TimeUnit;
import com.example.fitness.common.annotation.Idempotent;
import com.example.fitness.common.annotation.RateLimit;
import com.example.fitness.common.exception.BusinessException;
import com.example.fitness.common.result.ErrorCode;
import com.example.fitness.api.dto.UserDTO;
import com.example.fitness.user.mapper.UserMapper;
import com.example.fitness.user.model.entity.User;
import com.example.fitness.user.service.UserService;
import com.example.fitness.api.dto.LoginRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户服务实现类
 * 提供用户登录、首次使用落地流程管理和统计数据更新等功能。
 * 使用了 Redis 缓存方案降低数据库负载。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String USER_CACHE_KEY_PREFIX = "user:profile:";
    private static final long USER_CACHE_TTL = 3600; // 1小时缓存

    /**
     * 手机号登录：如果用户不存在则自动创建
     */
    @Override
    @RateLimit(count = 5, time = 1, limitType = RateLimit.LimitType.IP)
    public UserDTO loginByPhone(LoginRequest request) {
        String phone = request.getPhone();
        // @Valid validation handles null checks

        // 手机号自动加密查询（得益于 EncryptTypeHandler）
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getPhone, phone));

        // 用户不存在，执行注册逻辑
        if (user == null) {
            user = new User();
            user.setPhone(phone); // 自动加密存储
            user.setNickname("用户 " + phone.substring(phone.length() - 4));
            userMapper.insert(user);
        }

        return UserDTO.builder()
                .id(String.valueOf(user.getId()))
                .phone(user.getPhone()) // 自动解密返回
                .nickname(user.getNickname())
                .token("mock_jwt_token_" + user.getId())
                .build();
    }

    /**
     * 微信登录：Mock 实现
     */
    @Override
    @RateLimit(count = 5, time = 1, limitType = RateLimit.LimitType.IP)
    public UserDTO loginByWechat(LoginRequest request) {
        String code = request.getCode();
        // @Valid validation handles null checks

        String openId = "wx_" + code; // Mock 生成 OpenID

        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getOpenId, openId));
        if (user == null) {
            user = new User();
            user.setOpenId(openId);
            user.setNickname("微信用户");
            userMapper.insert(user);
        }

        return UserDTO.builder()
                .id(String.valueOf(user.getId()))
                .nickname(user.getNickname())
                .avatar("https://api.dicebear.com/7.x/avataaars/svg?seed=" + user.getId())
                .token("mock_wx_token_" + user.getId())
                .build();
    }

    /**
     * 首次使用落地逻辑：设置难度等级，并返回对应的评分配置
     */
    @Override
    @Idempotent(expire = 5)
    public Map<String, Object> onboarding(Map<String, Object> request) {
        String userId = (String) request.get("userId");
        if (userId == null) {
            throw new BusinessException(ErrorCode.USER_ID_REQUIRED);
        }
        String level = (String) request.getOrDefault("difficultyLevel", "novice");

        // 1. 尝试从缓存读取
        User user = getUserForProfile(userId);

        user.setDifficultyLevel(level);
        userMapper.updateById(user);

        // 2. 更新后清除缓存，保证数据一致性（Cache Pattern: Cache-Aside - Invalidate on Update）
        // 也可以选择更新缓存，但失效缓存通常更简单且并发问题更少
        String cacheKey = USER_CACHE_KEY_PREFIX + userId;
        redisTemplate.delete(cacheKey);

        Map<String, Object> config = new HashMap<>();
        // expert 难度下的评分容差更小
        config.put("scoringTolerance", "expert".equals(level) ? 5 : 20);
        config.put("recommendedPlan", "plan_starter");
        return config;
    }

    /**
     * 内部私有方法：带缓存查询用户信息
     * Cache-Aside 模式：
     * 1. 查缓存，命中则返回
     * 2. 未命中查 DB
     * 3. 回写缓存并返回
     */
    private User getUserForProfile(String userId) {
        String cacheKey = USER_CACHE_KEY_PREFIX + userId;

        // 1. 查缓存
        String cachedValue = redisTemplate.opsForValue().get(cacheKey);
        if (cachedValue != null && !cachedValue.isEmpty()) {
            try {
                // 缓存命中 (Cache Hit)
                log.debug("用户缓存命中: userId={}", userId);
                return objectMapper.readValue(cachedValue, User.class);
            } catch (JsonProcessingException e) {
                log.warn("用户缓存解析失败，将回退数据库查询: userId={}, error={}", userId, e.getMessage());
                // 解析失败视为未命中，继续查库覆盖脏数据
            }
        }

        // 2. 查数据库 (Cache Miss)
        log.debug("用户缓存未命中，查询数据库: userId={}", userId);
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 3. 回写缓存 (Write Back to Cache)
        try {
            String jsonValue = objectMapper.writeValueAsString(user);
            redisTemplate.opsForValue().set(cacheKey, jsonValue, USER_CACHE_TTL, TimeUnit.SECONDS);
        } catch (JsonProcessingException e) {
            log.error("用户数据序列化写入缓存失败: userId={}, error={}", userId, e.getMessage());
            // 写入缓存失败不影响主业务逻辑，仅记录日志
        }

        return user;
    }

    /**
     * 更新用户统计：实际场景中可能同步到 user_stats 表或 Apache Doris
     */
    @Override
    @Idempotent(expire = 3)
    public void updateUserStats(Map<String, Object> request) {
        if (request == null || request.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        log.info("正在更新用户统计信息: {}", request);
    }
}
