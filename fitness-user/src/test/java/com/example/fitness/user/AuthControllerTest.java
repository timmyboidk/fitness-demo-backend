package com.example.fitness.user;

import com.example.fitness.api.dto.*;
import com.example.fitness.common.util.JwtUtil;
import com.example.fitness.user.controller.AuthController;
import com.example.fitness.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 认证控制器单元测试
 */
@WebMvcTest(AuthController.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtUtil jwtUtil; // Mock JwtUtil for WebConfig/LoginInterceptor

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 测试请求验证码接口
     */
    @Test
    public void testRequestOtp() throws Exception {
        RequestOtpRequest request = new RequestOtpRequest();
        request.setPhone("13800138000");

        mockMvc.perform(post("/api/auth/request-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.expiresIn").value(60));
    }

    /**
     * 测试手机号验证码登录接口
     */
    @Test
    public void testVerifyOtp() throws Exception {
        UserDTO mockUser = UserDTO.builder().id("u1").nickname("test").token("t1").build();
        Mockito.when(userService.loginByPhone(any())).thenReturn(mockUser);

        VerifyOtpRequest request = new VerifyOtpRequest();
        request.setPhone("13800138000");
        request.setCode("1234");

        mockMvc.perform(post("/api/auth/verify-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("u1"));
    }

    /**
     * 测试微信登录接口
     */
    @Test
    public void testWechatLogin() throws Exception {
        UserDTO mockUser = UserDTO.builder().id("u2").nickname("wx").token("t2").build();
        Mockito.when(userService.loginByWechat(any())).thenReturn(mockUser);

        WechatLoginRequest request = new WechatLoginRequest();
        request.setCode("wx_auth_code_xyz");

        mockMvc.perform(post("/api/auth/wechat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("u2"));
    }

    /**
     * 测试手机号登录接口 (旧接口兼容)
     */
    @Test
    public void testLoginPhone() throws Exception {
        UserDTO mockUser = UserDTO.builder().id("u1").nickname("test").token("t1").build();
        Mockito.when(userService.loginByPhone(any())).thenReturn(mockUser);

        Map<String, Object> payload = new HashMap<>();
        payload.put("phone", "123");
        AuthRequest request = new AuthRequest();
        request.setType("login_phone");
        request.setPayload(payload);

        mockMvc.perform(post("/api/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("u1"));
    }

    /**
     * 测试微信登录接口 (旧接口兼容)
     */
    @Test
    public void testLoginWechat() throws Exception {
        UserDTO mockUser = UserDTO.builder().id("u2").nickname("wx").token("t2").build();
        Mockito.when(userService.loginByWechat(any())).thenReturn(mockUser);

        Map<String, Object> payload = new HashMap<>();
        payload.put("code", "wx123");
        AuthRequest request = new AuthRequest();
        request.setType("login_wechat");
        request.setPayload(payload);

        mockMvc.perform(post("/api/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("u2"));
    }

    /**
     * 测试首次使用设置接口
     */
    @Test
    public void testOnboarding() throws Exception {
        Map<String, Object> response = new HashMap<>();
        response.put("scoringTolerance", 20);
        response.put("recommendedPlan", "plan_starter");
        Mockito.when(userService.onboarding(any())).thenReturn(response);

        OnboardingRequest request = new OnboardingRequest();
        request.setUserId("u1");
        request.setDifficultyLevel("novice");

        mockMvc.perform(post("/api/auth/onboarding")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("updated"));
    }

    /**
     * 测试更新统计数据接口
     */
    @Test
    public void testUpdateStats() throws Exception {
        Mockito.doNothing().when(userService).updateUserStats(any());

        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", "u1");

        mockMvc.perform(post("/api/auth/user/stats")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    /**
     * 测试不支持的登录类型 (旧接口兼容)
     */
    @Test
    public void testLoginUnknownType() throws Exception {
        AuthRequest request = new AuthRequest();
        request.setType("unknown");
        request.setPayload(new HashMap<>());

        mockMvc.perform(post("/api/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("不支持的登录类型"));
    }
}
