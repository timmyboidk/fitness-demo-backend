package com.example.fitness.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.fitness.api.dto.UserDTO;
import com.example.fitness.user.mapper.UserMapper;
import com.example.fitness.user.model.entity.User;
import com.example.fitness.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    @Override
    public UserDTO loginByPhone(Map<String, Object> payload) {
        String phone = (String) payload.get("phone");
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getPhone, phone));

        if (user == null) {
            user = new User();
            user.setPhone(phone);
            user.setNickname("User " + phone.substring(phone.length() - 4));
            userMapper.insert(user);
        }

        return UserDTO.builder()
                .id(String.valueOf(user.getId()))
                .phone(user.getPhone())
                .nickname(user.getNickname())
                .token("mock_jwt_token_" + user.getId())
                .build();
    }

    @Override
    public UserDTO loginByWechat(Map<String, Object> payload) {
        String code = (String) payload.get("code");
        String openId = "wx_" + code; // Mock OpenID generation

        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getOpenId, openId));
        if (user == null) {
            user = new User();
            user.setOpenId(openId);
            user.setNickname("WeChat User");
            userMapper.insert(user);
        }

        return UserDTO.builder()
                .id(String.valueOf(user.getId()))
                .nickname(user.getNickname())
                .avatar("https://api.dicebear.com/7.x/avataaars/svg?seed=" + user.getId())
                .token("mock_wx_token_" + user.getId())
                .build();
    }

    @Override
    public Map<String, Object> onboarding(Map<String, Object> request) {
        String userId = (String) request.get("userId");
        String level = (String) request.getOrDefault("difficultyLevel", "novice");

        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setDifficultyLevel(level);
            userMapper.updateById(user);
        }

        Map<String, Object> config = new HashMap<>();
        config.put("scoringTolerance", "expert".equals(level) ? 5 : 20);
        config.put("recommendedPlan", "plan_starter");
        return config;
    }

    @Override
    public void updateUserStats(Map<String, Object> request) {
        // Implementation for stats persistence
        // In a real scenario, this might update a user_stats table or Apache Doris
        log.info("Updating user stats: {}", request);
    }
}
