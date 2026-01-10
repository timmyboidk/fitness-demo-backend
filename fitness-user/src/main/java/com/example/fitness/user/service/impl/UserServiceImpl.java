package com.example.fitness.user.service.impl;

import com.example.fitness.api.dto.UserDTO;
import com.example.fitness.user.service.UserService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    @Override
    public UserDTO loginByPhone(Map<String, Object> payload) {
        String phone = (String) payload.get("phone");
        return UserDTO.builder()
                .id("u_" + UUID.randomUUID().toString().substring(0, 8))
                .phone(phone)
                .nickname("User " + phone.substring(phone.length() - 4))
                .token("mock_jwt_token_" + UUID.randomUUID())
                .build();
    }

    @Override
    public UserDTO loginByWechat(Map<String, Object> payload) {
        return UserDTO.builder()
                .id("u_wx_" + UUID.randomUUID().toString().substring(0, 8))
                .nickname("WeChat User")
                .avatar("https://api.dicebear.com/7.x/avataaars/svg?seed=Felix")
                .token("mock_wx_token_" + UUID.randomUUID())
                .build();
    }

    @Override
    public Map<String, Object> onboarding(Map<String, Object> request) {
        Map<String, Object> config = new HashMap<>();
        String level = (String) request.getOrDefault("difficultyLevel", "novice");
        config.put("scoringTolerance", "expert".equals(level) ? 5 : 20);
        config.put("recommendedPlan", "plan_starter");
        return config;
    }
}
