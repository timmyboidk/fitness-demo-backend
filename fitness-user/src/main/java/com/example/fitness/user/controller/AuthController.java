package com.example.fitness.user.controller;

import com.example.fitness.api.dto.UserDTO;
import com.example.fitness.common.result.Result;
import com.example.fitness.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证控制器 - 处理用户登录、首次使用落地流程及数据统计
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;

    /**
     * 用户登录接口
     * 
     * @param request 包含登录类型（手机号/微信）及负载数据
     * @return 返回用户DTO信息
     */
    @PostMapping
    public Result<UserDTO> login(@RequestBody com.example.fitness.api.dto.LoginRequest request) {
        if ("login_phone".equals(request.getType())) {
            return Result.success(userService.loginByPhone(request));
        } else if ("login_wechat".equals(request.getType())) {
            return Result.success(userService.loginByWechat(request));
        }
        return Result.error("不支持的登录类型");
    }

    /**
     * 首次使用落地流程/首次设置接口
     * 
     * @param request 包含用户ID和难度等级
     * @return 返回系统配置（如评分容差）
     */
    @PostMapping("/onboarding")
    public Result<Map<String, Object>> onboarding(@RequestBody Map<String, Object> request) {
        return Result.success(userService.onboarding(request));
    }

    /**
     * 更新用户统计数据
     * 
     * @param request 负载数据
     */
    @PostMapping("/user/stats")
    public Result<Void> updateStats(@RequestBody Map<String, Object> request) {
        userService.updateUserStats(request);
        return Result.success(null);
    }
}
