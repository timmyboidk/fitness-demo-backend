package com.example.fitness.user;

import com.example.fitness.api.dto.AuthRequest;
import com.example.fitness.api.dto.UserDTO;
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

@WebMvcTest(AuthController.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

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
}
