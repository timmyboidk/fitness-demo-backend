package com.example.fitness.user;

import com.example.fitness.api.dto.UserDTO;
import com.example.fitness.user.mapper.UserMapper;
import com.example.fitness.user.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    public void testLoginByPhone() {
        // Mock data
        Map<String, Object> payload = new HashMap<>();
        payload.put("phone", "13800138000");

        // When
        // Mock selectOne to return null (new user) or existing user
        // Mockito.when(userMapper.selectOne(any())).thenReturn(null);
        // Strict stubbing might require matching arguments.
        // For simple test, if selectOne is called, it returns null by default for
        // mocks.

        // Act
        UserDTO result = userService.loginByPhone(payload);

        // Assert
        Assertions.assertNotNull(result);
        Assertions.assertEquals("13800138000", result.getPhone());
    }

    @Test
    public void testOnboarding() {
        Map<String, Object> req = new HashMap<>();
        req.put("difficultyLevel", "expert");

        Map<String, Object> config = userService.onboarding(req);
        Assertions.assertEquals(5, config.get("scoringTolerance"));
    }
}
