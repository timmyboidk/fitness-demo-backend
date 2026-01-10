package com.example.fitness.user.service;

import com.example.fitness.api.dto.UserDTO;
import java.util.Map;

public interface UserService {
    UserDTO loginByPhone(Map<String, Object> payload);

    UserDTO loginByWechat(Map<String, Object> payload);

    Map<String, Object> onboarding(Map<String, Object> request);
}
