package com.example.fitness.user.controller;

import com.example.fitness.api.dto.*;
import com.example.fitness.common.result.Result;
import com.example.fitness.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器 - 处理用户登录、首次使用落地流程及数据统计
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "认证模块", description = "用户登录、注册和身份验证接口")
public class AuthController {
    private final UserService userService;

    /**
     * 请求验证码接口
     * 
     * @param request 包含手机号
     * @return 返回验证码过期时间
     */
    @Operation(summary = "获取验证码", description = "发送短信验证码到指定手机号")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "操作成功"),
            @ApiResponse(responseCode = "400", description = "参数校验失败")
    })
    @PostMapping("/request-otp")
    public Result<Map<String, Object>> requestOtp(@Valid @RequestBody RequestOtpRequest request) {
        log.info("请求验证码: phone={}", request.getPhone());

        // 模拟发送验证码（生产环境应对接短信服务）
        // 并将验证码存入 Redis: redisTemplate.opsForValue().set("sms:code:" + phone, code, 60,
        // TimeUnit.SECONDS);

        Map<String, Object> data = new HashMap<>();
        data.put("expiresIn", 60);
        return Result.success(data);
    }

    /**
     * 手机号验证码登录接口
     * 
     * @param request 包含手机号和验证码
     * @return 返回用户DTO信息
     */
    @Operation(summary = "手机号验证登录", description = "使用手机号和验证码进行登录")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "登录成功"),
            @ApiResponse(responseCode = "400", description = "参数校验失败"),
            @ApiResponse(responseCode = "401", description = "验证码错误")
    })
    @PostMapping("/verify-otp")
    public Result<UserDTO> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        // 将请求转换为 LoginRequest 调用现有服务
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setPhone(request.getPhone());
        loginRequest.setCode(request.getCode());
        loginRequest.setType("phone");
        return Result.success(userService.loginByPhone(loginRequest));
    }

    /**
     * 微信授权登录接口
     * 
     * @param request 包含微信授权code
     * @return 返回用户DTO信息
     */
    @Operation(summary = "微信授权登录", description = "使用微信授权code进行登录")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "登录成功"),
            @ApiResponse(responseCode = "400", description = "参数校验失败"),
            @ApiResponse(responseCode = "401", description = "微信授权失败")
    })
    @PostMapping("/wechat")
    public Result<UserDTO> wechatLogin(@Valid @RequestBody WechatLoginRequest request) {
        // 将请求转换为 LoginRequest 调用现有服务
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setCode(request.getCode());
        loginRequest.setType("wechat");
        return Result.success(userService.loginByWechat(loginRequest));
    }

    /**
     * 用户登录接口 (已废弃，保留向后兼容)
     * 
     * @param request 包含登录类型（手机号/微信）及负载数据
     * @return 返回用户DTO信息
     * @deprecated 建议使用 /verify-otp 或 /wechat
     */
    @Deprecated
    @Operation(summary = "统一登录入口（已废弃）", description = "旧版统一登录接口，请使用 /verify-otp 或 /wechat")
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
    @Operation(summary = "用户新手引导设置", description = "设置用户难度等级等新手引导配置")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "操作成功"),
            @ApiResponse(responseCode = "400", description = "参数校验失败"),
            @ApiResponse(responseCode = "404", description = "用户不存在")
    })
    @PostMapping("/onboarding")
    public Result<Map<String, Object>> onboarding(@Valid @RequestBody OnboardingRequest request) {
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("userId", request.getUserId());
        requestMap.put("difficultyLevel", request.getDifficultyLevel());

        Map<String, Object> result = userService.onboarding(requestMap);
        result.put("status", "updated");
        return Result.success(result);
    }

    /**
     * 更新用户统计数据
     * 
     * @param request 负载数据
     */
    @Operation(summary = "更新用户统计数据", description = "更新用户训练统计信息")
    @PostMapping("/user/stats")
    public Result<Void> updateStats(@RequestBody Map<String, Object> request) {
        userService.updateUserStats(request);
        return Result.success(null);
    }
}
