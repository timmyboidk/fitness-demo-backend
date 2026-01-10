package com.example.fitness.user.service;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.api.WxMaUserService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import com.example.fitness.api.dto.LoginRequest;
import com.example.fitness.api.dto.UserDTO;
import com.example.fitness.common.util.JwtUtil;
import com.example.fitness.user.mapper.UserMapper;
import com.example.fitness.user.model.entity.User;
import com.example.fitness.user.service.impl.UserServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 用户服务单元测试
 */
@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private WxMaService wxMaService;

    @Mock
    private WxMaUserService wxMaUserService;

    @InjectMocks
    private UserServiceImpl userService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testLoginByPhone_NewUser() {
        // Setup
        LoginRequest req = new LoginRequest();
        req.setPhone("13800138000");
        req.setCode("1234");

        when(userMapper.selectOne(any())).thenReturn(null); // User not found
        when(jwtUtil.generateToken(anyString())).thenReturn("mock-token");
        when(userMapper.insert(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(100L); // simulate database generating ID
            return 1;
        });

        // Execute
        UserDTO result = userService.loginByPhone(req);

        // Verify
        assertNotNull(result);
        assertEquals("100", result.getId());
        assertEquals("mock-token", result.getToken());
        verify(userMapper, times(1)).insert(any(User.class));
    }

    @Test
    public void testLoginByPhone_ExistingUser() {
        // Setup
        LoginRequest req = new LoginRequest();
        req.setPhone("13800138000");

        User existingUser = new User();
        existingUser.setId(200L);
        existingUser.setPhone("13800138000");
        existingUser.setNickname("OldUser");

        when(userMapper.selectOne(any())).thenReturn(existingUser);
        when(jwtUtil.generateToken("200")).thenReturn("mock-existing-token");

        // Execute
        UserDTO result = userService.loginByPhone(req);

        // Verify
        assertNotNull(result);
        assertEquals("200", result.getId());
        assertEquals("mock-existing-token", result.getToken());
        verify(userMapper, times(0)).insert(any(User.class));
    }

    @Test
    public void testLoginByWechat_Success() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setCode("wx-code");

        WxMaJscode2SessionResult session = new WxMaJscode2SessionResult();
        session.setOpenid("mock-openid");

        when(wxMaService.getUserService()).thenReturn(wxMaUserService);
        when(wxMaUserService.getSessionInfo("wx-code")).thenReturn(session);
        when(userMapper.selectOne(any())).thenReturn(null); // New user
        when(jwtUtil.generateToken(anyString())).thenReturn("wx-token");
        when(userMapper.insert(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(300L);
            return 1;
        });

        UserDTO result = userService.loginByWechat(req);

        assertEquals("300", result.getId());
        assertEquals("wx-token", result.getToken());
        verify(wxMaUserService).getSessionInfo("wx-code");
        verify(userMapper).insert(any(User.class));
    }
}
