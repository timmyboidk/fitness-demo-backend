package com.example.fitness;

import com.example.fitness.common.util.JwtUtil;
import com.example.fitness.user.model.entity.User;
import com.example.fitness.user.mapper.UserMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

@AutoConfigureMockMvc
@DisplayName("认证模块全链路集成测试")
class AuthFullFlowIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    @DisplayName("完整登录流程：手机号登录 -> 获取Token -> 访问受保护接口")
    void loginFlow_ShouldSucceed() throws Exception {
        // 1. 模拟手机号登录请求
        String loginRequest = """
                {
                    "phone": "13900001111",
                    "code": "123456"
                }
                """;

        MvcResult loginResult = mockMvc.perform(post("/api/auth/verify-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").exists())
                .andReturn();

        // 提取 Token
        String responseBody = loginResult.getResponse().getContentAsString();
        String token = com.jayway.jsonpath.JsonPath.read(responseBody, "$.data.token");
        assertThat(token).isNotBlank();

        // 2. 验证 Redis 中存储了 Token (假设 Token 存储 key 格式为 "token:userId:tokenValue" 或者其他逻辑)
        // 这里我们简单验证 Token 有效性
        String userId = jwtUtil.getUserIdFromToken(token);
        assertThat(userId).isNotNull();

        // 3. 验证数据库中用户已创建
        User user = userMapper.selectById(Long.valueOf(userId));
        assertThat(user).isNotNull();
        // 验证其他字段 (Phone 可能被加密存储，此处验证 Nickname)
        assertThat(user.getNickname()).isEqualTo("User_1111");

        // 4. 使用 Token 访问受保护接口 (获取用户信息)
        mockMvc.perform(get("/api/user/profile")
                .header("Authorization", "Bearer " + token))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.nickname").value("User_1111"));
    }
}
