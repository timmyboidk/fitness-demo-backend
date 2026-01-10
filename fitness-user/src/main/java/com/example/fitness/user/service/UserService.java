package com.example.fitness.user.service;

import com.example.fitness.api.dto.UserDTO;
import java.util.Map;

public interface UserService {
    /**
     * 手机号登录
     */
    UserDTO loginByPhone(Map<String, Object> payload);

    /**
     * 微信登录
     */
    UserDTO loginByWechat(Map<String, Object> payload);

    /**
     * 入职流程设置（如难度等级）
     */
    Map<String, Object> onboarding(Map<String, Object> request);

    /**
     * 更新用户统计信息
     */
    void updateUserStats(Map<String, Object> request);
}
