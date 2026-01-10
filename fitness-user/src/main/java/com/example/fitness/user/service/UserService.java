package com.example.fitness.user.service;

import com.example.fitness.api.dto.LoginRequest;
import com.example.fitness.api.dto.UserDTO;
import java.util.Map;

/**
 * 用户服务接口
 * 定义了用户核心业务逻辑，包括多渠道登录和个人设置。
 */
public interface UserService {
    /**
     * 手机号登录
     */
    UserDTO loginByPhone(LoginRequest request);

    /**
     * 微信登录
     */
    UserDTO loginByWechat(LoginRequest request);

    /**
     * 首次使用落地流程设置（如难度等级）
     */
    Map<String, Object> onboarding(Map<String, Object> request);

    /**
     * 更新用户统计信息
     */
    void updateUserStats(Map<String, Object> request);
}
