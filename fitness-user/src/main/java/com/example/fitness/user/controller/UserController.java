package com.example.fitness.user.controller;

import com.example.fitness.api.dto.UserDTO;
import com.example.fitness.common.result.Result;
import com.example.fitness.common.util.JwtUtil;
import com.example.fitness.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户控制器
 * 
 * <p>
 * 提供用户个人信息查询相关的 RESTful API 接口，包括：
 * <ul>
 * <li>获取个人资料</li>
 * <li>获取训练统计数据</li>
 * </ul>
 * 
 * <p>
 * 所有接口需要 JWT 认证，通过 Authorization Header 传递 Token。
 * 
 * @author fitness-team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Tag(name = "用户模块", description = "用户个人信息及统计数据")
public class UserController {

    /** 用户服务，处理用户相关业务逻辑 */
    private final UserService userService;

    /** JWT 工具类，用于解析和验证 Token */
    private final JwtUtil jwtUtil;

    /**
     * 获取当前用户个人资料
     * 
     * <p>
     * 从 JWT Token 中提取用户 ID，查询并返回用户详细信息。
     * 
     * @param request HTTP 请求对象，用于获取 Authorization Header
     * @return 包含用户信息的响应结果
     * @throws com.example.fitness.common.exception.BusinessException 用户未登录时抛出
     */
    @GetMapping("/profile")
    @Operation(summary = "获取个人资料", description = "获取当前登录用户的详细信息")
    public Result<UserDTO> getProfile(HttpServletRequest request) {
        String userId = getUserIdFromRequest(request);
        return Result.success(userService.getUserProfile(Long.valueOf(userId)));
    }

    /**
     * 从 HTTP 请求中提取用户 ID
     * 
     * <p>
     * 解析 Authorization Header 中的 Bearer Token，提取用户标识。
     * 
     * @param request HTTP 请求对象
     * @return 用户 ID 字符串
     * @throws com.example.fitness.common.exception.BusinessException Token 无效或缺失时抛出
     */
    private String getUserIdFromRequest(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            return jwtUtil.getUserIdFromToken(token);
        }
        // Token 缺失或格式错误，抛出未授权异常
        throw new com.example.fitness.common.exception.BusinessException(
                com.example.fitness.common.result.ErrorCode.UNAUTHORIZED, "用户未登录");
    }
}
