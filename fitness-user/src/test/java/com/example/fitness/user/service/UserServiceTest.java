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
import org.junit.jupiter.api.Test;
import java.util.Map;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

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

    @Test
    public void testLoginByPhone_ConcurrentRegistration_Idempotency() {
        // Setup
        LoginRequest req = new LoginRequest();
        req.setPhone("13900000000");

        User existingUser = new User();
        existingUser.setId(400L);
        existingUser.setPhone("13900000000");

        when(userMapper.selectOne(any())).thenReturn(null) // First check returns null
                .thenReturn(existingUser); // Second check (after exception) returns found user

        when(jwtUtil.generateToken(anyString())).thenReturn("idempotent-token");
        when(userMapper.insert(any(User.class)))
                .thenThrow(new org.springframework.dao.DataIntegrityViolationException(
                        "Duplicate entry '13900000000' for key 'user.phone'"));

        // Execute
        UserDTO result = userService.loginByPhone(req);

        // Verify
        assertNotNull(result);
        assertEquals("400", result.getId());
        assertEquals("idempotent-token", result.getToken());
        verify(userMapper, times(2)).selectOne(any()); // Called twice
    }

    @Test
    public void testOnboarding_Success() {
        Map<String, Object> req = new java.util.HashMap<>();
        req.put("userId", "500");
        req.put("difficultyLevel", "expert");

        User user = new User();
        user.setId(500L);

        // Mock getUserForProfile
        // Cache miss
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);
        when(userMapper.selectById("500")).thenReturn(user);

        // Execute
        Map<String, Object> result = userService.onboarding(req);

        // Verify
        assertNotNull(result);
        assertEquals(5, result.get("scoringTolerance")); // expert -> 5
        verify(userMapper).updateById(user);
        verify(redisTemplate).delete(anyString());
    }

    @Test
    public void testUpdateUserStats() {
        Map<String, Object> req = new java.util.HashMap<>();
        req.put("userId", "600");
        userService.updateUserStats(req);
        // Just verify no exception
    }

    @Test
    public void testLoginByWechat_Failure() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setCode("bad-code");

        when(wxMaService.getUserService()).thenReturn(wxMaUserService);
        when(wxMaUserService.getSessionInfo("bad-code"))
                .thenThrow(new me.chanjar.weixin.common.error.WxErrorException("Failed"));

        try {
            userService.loginByWechat(req);
        } catch (com.example.fitness.common.exception.BusinessException e) {
            assertEquals(1004, e.getCode()); // LOGIN_FAILED
        }
    }

    @Test
    public void testUpdateUserStats_Null() {
        try {
            userService.updateUserStats(null);
        } catch (com.example.fitness.common.exception.BusinessException e) {
            assertEquals(400, e.getCode()); // PARAM_ERROR
        }
    }
}
