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

/**
 * 用户服务实现类
 * 提供用户登录、首次使用落地流程管理和统计数据更新等功能。
 * 使用了 Redis 缓存方案降低数据库负载。
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

    private static final String USER_CACHE_KEY_PREFIX = "user:profile:";
    private static final long USER_CACHE_TTL = 3600; // 1小时缓存

    /**
     * 手机号登录：如果用户不存在则自动创建
     */
    @Override
    @RateLimit(count = 5, time = 1, limitType = RateLimit.LimitType.IP)
    public UserDTO loginByPhone(LoginRequest request) {
        // 1. 校验验证码 (开发环境跳过，生产环境应比对 Redis)
        // String cachedCode = redisTemplate.opsForValue().get("sms:code:" +
        // request.getPhone());
        // if (!request.getCode().equals(cachedCode)) throw new
        // BusinessException(ErrorCode.PARAM_ERROR, "验证码错误");

        // 2. 查询或注册用户
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getPhone, request.getPhone()));
        if (user == null) {
            user = new User();
            user.setPhone(request.getPhone());
            user.setNickname("User_" + request.getPhone().substring(7));
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            try {
                userMapper.insert(user);
            } catch (org.springframework.dao.DuplicateKeyException e) {
                // 并发注册处理：虽然 user == null，但 insert 时遇到 duplicate key
                // 重新查询用户并返回
                User existingUser = userMapper
                        .selectOne(new LambdaQueryWrapper<User>().eq(User::getPhone, request.getPhone()));
                if (existingUser != null) {
                    user = existingUser;
                } else {
                    throw e; // 理论上不应到达此处，抛出异常
                }
            }
        }

        // 3. 生成真实 JWT Token
        String token = jwtUtil.generateToken(String.valueOf(user.getId()));

        // 4. 封装返回
        return convertToDTO(user, token);
    }

    /**
     * 微信登录
     */
    @Override
    @RateLimit(count = 5, time = 1, limitType = RateLimit.LimitType.IP)
    public UserDTO loginByWechat(LoginRequest request) {
        String openId;
        try {
            // 1. 调用微信接口获取 openId
            WxMaJscode2SessionResult session = wxMaService.getUserService().getSessionInfo(request.getCode());
            openId = session.getOpenid();
        } catch (WxErrorException e) {
            log.error("微信登录失败: {}", e.getMessage());
            throw new BusinessException(ErrorCode.LOGIN_FAILED, "微信授权失败");
        }

        // 2. 查询或注册用户
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getOpenId, openId));
        if (user == null) {
            user = new User();
            user.setOpenId(openId);
            user.setNickname("Wechat User");
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            userMapper.insert(user);
        }

        // 3. 生成真实 Token
        String token = jwtUtil.generateToken(String.valueOf(user.getId()));

        return convertToDTO(user, token);
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

    private UserDTO convertToDTO(User user, String token) {
        return UserDTO.builder()
                .id(String.valueOf(user.getId()))
                .phone(user.getPhone())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .token(token)
                .build();
    }
}
