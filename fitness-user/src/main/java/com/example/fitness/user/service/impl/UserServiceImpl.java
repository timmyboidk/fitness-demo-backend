package com.example.fitness.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.fitness.common.exception.BusinessException;
import com.example.fitness.common.result.ErrorCode;
import com.example.fitness.api.dto.UserDTO;
import com.example.fitness.user.mapper.UserMapper;
import com.example.fitness.user.model.entity.User;
import com.example.fitness.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    /**
     * 手机号登录：如果用户不存在则自动创建
     */
    @Override
    public UserDTO loginByPhone(Map<String, Object> payload) {
        String phone = (String) payload.get("phone");
        if (phone == null || phone.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getPhone, phone));

        // 用户不存在，执行注册逻辑
        if (user == null) {
            user = new User();
            user.setPhone(phone);
            user.setNickname("用户 " + phone.substring(phone.length() - 4));
            userMapper.insert(user);
        }

        return UserDTO.builder()
                .id(String.valueOf(user.getId()))
                .phone(user.getPhone())
                .nickname(user.getNickname())
                .token("mock_jwt_token_" + user.getId())
                .build();
    }

    /**
     * 微信登录：Mock 实现
     */
    @Override
    public UserDTO loginByWechat(Map<String, Object> payload) {
        String code = (String) payload.get("code");
        if (code == null || code.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
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
     * 入职逻辑：设置难度等级，并返回对应的评分配置
     */
    @Override
    public Map<String, Object> onboarding(Map<String, Object> request) {
        String userId = (String) request.get("userId");
        if (userId == null) {
            throw new BusinessException(ErrorCode.USER_ID_REQUIRED);
        }
        String level = (String) request.getOrDefault("difficultyLevel", "novice");

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        user.setDifficultyLevel(level);
        userMapper.updateById(user);

        Map<String, Object> config = new HashMap<>();
        // expert 难度下的评分容差更小
        config.put("scoringTolerance", "expert".equals(level) ? 5 : 20);
        config.put("recommendedPlan", "plan_starter");
        return config;
    }

    /**
     * 更新用户统计：实际场景中可能同步到 user_stats 表或 Apache Doris
     */
    @Override
    public void updateUserStats(Map<String, Object> request) {
        if (request == null || request.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        log.info("正在更新用户统计信息: {}", request);
    }
}
