package com.example.fitness.user.interceptor;

import com.example.fitness.common.result.ErrorCode;
import com.example.fitness.common.exception.BusinessException;
import com.example.fitness.common.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录拦截器
 * 校验请求头中的 Authorization: Bearer <token>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LoginInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        // 放行 Swagger 和 静态资源 (在 WebConfig 中已排除，此处作为双重保障)
        String uri = request.getRequestURI();
        // 忽略大小写检查
        String lowerUri = uri.toLowerCase();
        if (lowerUri.contains("swagger") || lowerUri.contains("api-docs") || lowerUri.contains("webjars")
                || lowerUri.contains("/error")) {
            return true;
        }

        // 获取 Token
        String token = request.getHeader("Authorization");
        if (!StringUtils.hasText(token)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        // 移除 Bearer 前缀
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        try {
            // 验证 Token
            jwtUtil.validateToken(token);
            // 可以将 userId 放入 request attribute 或 ThreadLocal
            String userId = jwtUtil.getUserIdFromToken(token);
            request.setAttribute("userId", userId);

            return true;
        } catch (Exception e) {
            log.warn("Token 验证失败: {}", e.getMessage());
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
    }
}
