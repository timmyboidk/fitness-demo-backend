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

@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Tag(name = "用户模块", description = "用户个人信息及统计数据")
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    @GetMapping("/profile")
    @Operation(summary = "获取个人资料", description = "获取当前登录用户的详细信息")
    public Result<UserDTO> getProfile(HttpServletRequest request) {
        String userId = getUserIdFromRequest(request);
        return Result.success(userService.getUserProfile(Long.valueOf(userId)));
    }

    private String getUserIdFromRequest(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            return jwtUtil.getUserIdFromToken(token);
        }
        // Fallback or error
        throw new com.example.fitness.common.exception.BusinessException(
                com.example.fitness.common.result.ErrorCode.UNAUTHORIZED, "用户未登录");
    }
}
