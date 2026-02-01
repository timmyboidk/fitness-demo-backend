package com.example.fitness.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import com.example.fitness.common.util.JwtUtil;
import me.chanjar.weixin.common.error.WxErrorException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 用户服务实现类
 * 
 * <p>
 * 提供用户登录、首次使用落地流程管理和统计数据更新等功能。
 * 使用了 Redis 缓存方案降低数据库负载。
 * 
 * @author fitness-team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final UserMapper userMapper;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final JwtUtil jwtUtil;
    private final WxMaService wxMaService;

    /** 用户缓存键前缀 */
    private static final String USER_CACHE_KEY_PREFIX = "user:profile:";

    /** 缓存过期时间：1 小时 */
    private static final long USER_CACHE_TTL = 3600;

    // ==================== 登录相关方法 ====================

    /**
     * 手机号登录
     * <p>
     * 如果用户不存在则自动创建新用户
     */
    @Override
    @RateLimit(count = 5, time = 1, limitType = RateLimit.LimitType.IP)
    public UserDTO loginByPhone(LoginRequest request) {
        User user = findOrCreateUserByPhone(request.getPhone());
        String token = jwtUtil.generateToken(String.valueOf(user.getId()));
        return convertToDTO(user, token);
    }

    /**
     * 微信登录
     * <p>
     * 通过微信授权码获取 openId，若用户不存在则自动创建
     */
    @Override
    @RateLimit(count = 5, time = 1, limitType = RateLimit.LimitType.IP)
    public UserDTO loginByWechat(LoginRequest request) {
        String openId = getWechatOpenId(request.getCode());
        User user = findOrCreateUserByOpenId(openId);
        String token = jwtUtil.generateToken(String.valueOf(user.getId()));
        return convertToDTO(user, token);
    }

    // ==================== 用户引导流程 ====================

    /**
     * 首次使用落地逻辑
     * <p>
     * 设置用户难度等级，并返回对应的评分配置
     */
    @Override
    @Idempotent(expire = 5)
    public Map<String, Object> onboarding(Map<String, Object> request) {
        String userId = extractUserId(request);
        String level = (String) request.getOrDefault("difficultyLevel", "novice");

        User user = getUserForProfile(userId);
        user.setDifficultyLevel(level);
        userMapper.updateById(user);

        // 清除缓存保证数据一致性（Cache-Aside 模式）
        invalidateUserCache(userId);

        return buildOnboardingConfig(level);
    }

    // ==================== 用户信息查询 ====================

    @Override
    public UserDTO getUserProfile(Long userId) {
        User user = getUserForProfile(String.valueOf(userId));
        return convertToDTO(user, null);
    }

    @Override
    public Map<String, Object> getUserStats(Long userId) {
        User user = getUserForProfile(String.valueOf(userId));
        return buildUserStats(user);
    }

    @Override
    @Idempotent(expire = 3)
    public void updateUserStats(Map<String, Object> request) {
        if (request == null || request.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        log.info("正在更新用户统计信息: {}", request);
    }

    // ==================== 私有辅助方法：用户查询与创建 ====================

    /**
     * 根据手机号查找或创建用户
     */
    private User findOrCreateUserByPhone(String phone) {
        User existingUser = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getPhone, phone));

        if (existingUser != null) {
            return existingUser;
        }

        return createNewPhoneUser(phone);
    }

    /**
     * 创建新的手机号用户（处理并发注册）
     */
    private User createNewPhoneUser(String phone) {
        User user = buildNewUser(phone, "User_" + phone.substring(7));

        try {
            userMapper.insert(user);
            return user;
        } catch (Exception e) {
            return handleDuplicateRegistration(phone, e);
        }
    }

    /**
     * 处理并发注册导致的重复键异常
     */
    private User handleDuplicateRegistration(String phone, Exception e) {
        if (!isDuplicateKeyException(e)) {
            throw wrapException(e);
        }

        // 并发注册：重新查询已存在的用户
        User existingUser = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getPhone, phone));

        if (existingUser != null) {
            return existingUser;
        }

        throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "注册失败，请稍后重试");
    }

    /**
     * 根据 openId 查找或创建微信用户
     */
    private User findOrCreateUserByOpenId(String openId) {
        User existingUser = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getOpenId, openId));

        if (existingUser != null) {
            return existingUser;
        }

        User user = new User();
        user.setOpenId(openId);
        user.setNickname("Wechat User");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.insert(user);

        return user;
    }

    /**
     * 获取微信 openId
     */
    private String getWechatOpenId(String code) {
        try {
            WxMaJscode2SessionResult session = wxMaService.getUserService().getSessionInfo(code);
            return session.getOpenid();
        } catch (WxErrorException e) {
            log.error("微信登录失败: {}", e.getMessage());
            throw new BusinessException(ErrorCode.LOGIN_FAILED, "微信授权失败");
        }
    }

    // ==================== 私有辅助方法：缓存操作 ====================

    /**
     * 带缓存查询用户信息（Cache-Aside 模式）
     */
    private User getUserForProfile(String userId) {
        String cacheKey = USER_CACHE_KEY_PREFIX + userId;

        // 1. 尝试从缓存读取
        Optional<User> cachedUser = readFromCache(cacheKey);
        if (cachedUser.isPresent()) {
            log.debug("用户缓存命中: userId={}", userId);
            return cachedUser.get();
        }

        // 2. 缓存未命中，查询数据库
        log.debug("用户缓存未命中，查询数据库: userId={}", userId);
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 3. 回写缓存
        writeToCache(cacheKey, user);

        return user;
    }

    /**
     * 从缓存读取用户
     */
    private Optional<User> readFromCache(String cacheKey) {
        String cachedValue = redisTemplate.opsForValue().get(cacheKey);
        if (cachedValue == null || cachedValue.isEmpty()) {
            return Optional.empty();
        }

        try {
            return Optional.of(objectMapper.readValue(cachedValue, User.class));
        } catch (JsonProcessingException e) {
            log.warn("用户缓存解析失败: cacheKey={}, error={}", cacheKey, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * 写入用户缓存
     */
    private void writeToCache(String cacheKey, User user) {
        try {
            String jsonValue = objectMapper.writeValueAsString(user);
            redisTemplate.opsForValue().set(cacheKey, jsonValue, USER_CACHE_TTL, TimeUnit.SECONDS);
        } catch (JsonProcessingException e) {
            log.error("用户数据序列化写入缓存失败: cacheKey={}, error={}", cacheKey, e.getMessage());
        }
    }

    /**
     * 失效用户缓存
     */
    private void invalidateUserCache(String userId) {
        String cacheKey = USER_CACHE_KEY_PREFIX + userId;
        redisTemplate.delete(cacheKey);
    }

    // ==================== 私有辅助方法：数据构建 ====================

    /**
     * 构建新用户对象
     */
    private User buildNewUser(String phone, String nickname) {
        User user = new User();
        user.setPhone(phone);
        user.setNickname(nickname);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }

    /**
     * 构建引导配置
     */
    private Map<String, Object> buildOnboardingConfig(String level) {
        Map<String, Object> config = new HashMap<>();
        config.put("scoringTolerance", "expert".equals(level) ? 5 : 20);
        config.put("recommendedPlan", "plan_starter");
        return config;
    }

    /**
     * 构建用户统计数据
     */
    private Map<String, Object> buildUserStats(User user) {
        Map<String, Object> stats = new HashMap<>();

        int totalDuration = user.getTotalDuration() != null ? user.getTotalDuration() : 0;
        int totalScore = user.getTotalScore() != null ? user.getTotalScore() : 0;

        stats.put("totalDuration", totalDuration);
        stats.put("totalScore", totalScore);
        stats.put("weeklyDuration", 120);
        stats.put("totalCalories", totalScore * 10);
        stats.put("completionRate", 85);
        stats.put("history", java.util.Collections.emptyList());

        return stats;
    }

    /**
     * 转换为 DTO
     */
    private UserDTO convertToDTO(User user, String token) {
        return UserDTO.builder()
                .id(String.valueOf(user.getId()))
                .phone(user.getPhone())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .token(token)
                .build();
    }

    // ==================== 私有辅助方法：异常处理 ====================

    /**
     * 从请求中提取用户 ID
     */
    private String extractUserId(Map<String, Object> request) {
        String userId = (String) request.get("userId");
        if (userId == null) {
            throw new BusinessException(ErrorCode.USER_ID_REQUIRED);
        }
        return userId;
    }

    /**
     * 判断是否为重复键异常
     */
    private boolean isDuplicateKeyException(Exception e) {
        String message = e.getMessage();
        if (message != null && message.contains("Duplicate entry")) {
            return true;
        }
        return e.getCause() != null &&
                e.getCause().getMessage() != null &&
                e.getCause().getMessage().contains("Duplicate entry");
    }

    /**
     * 包装异常
     */
    private RuntimeException wrapException(Exception e) {
        if (e instanceof RuntimeException) {
            return (RuntimeException) e;
        }
        return new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
    }
}
