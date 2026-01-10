package com.example.fitness.user;

import com.example.fitness.api.dto.UserDTO;
import com.example.fitness.user.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class UserServiceTest {

    private final UserServiceImpl userService = new UserServiceImpl();

    @Test
    public void testLoginByPhone() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("phone", "13800000000");

        UserDTO user = userService.loginByPhone(payload);
        Assertions.assertNotNull(user);
        Assertions.assertNotNull(user.getId());
        Assertions.assertEquals("13800000000", user.getPhone());
    }

    @Test
    public void testOnboarding() {
        Map<String, Object> req = new HashMap<>();
        req.put("difficultyLevel", "expert");

        Map<String, Object> config = userService.onboarding(req);
        Assertions.assertEquals(5, config.get("scoringTolerance"));
    }
}
