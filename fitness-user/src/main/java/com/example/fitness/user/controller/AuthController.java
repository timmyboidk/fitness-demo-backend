package com.example.fitness.user.controller;

import com.example.fitness.api.dto.AuthRequest;
import com.example.fitness.api.dto.UserDTO;
import com.example.fitness.common.result.Result;
import com.example.fitness.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;

    @PostMapping
    public Result<UserDTO> login(@RequestBody AuthRequest request) {
        if ("login_phone".equals(request.getType())) {
            return Result.success(userService.loginByPhone(request.getPayload()));
        } else if ("login_wechat".equals(request.getType())) {
            return Result.success(userService.loginByWechat(request.getPayload()));
        }
        return Result.error("Unsupported login type");
    }

    @PostMapping("/onboarding")
    public Result<Map<String, Object>> onboarding(@RequestBody Map<String, Object> request) {
        return Result.success(userService.onboarding(request));
    }

    @PostMapping("/user/stats")
    public Result<Void> updateStats(@RequestBody Map<String, Object> request) {
        userService.updateUserStats(request);
        return Result.success(null);
    }
}
