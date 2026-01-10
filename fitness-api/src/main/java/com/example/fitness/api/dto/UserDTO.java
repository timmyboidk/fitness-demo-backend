package com.example.fitness.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户数据传输对象 - 用于接口返回用户信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private String id; // 用户 ID
    private String nickname; // 昵称
    private String phone; // 手机号
    private String avatar; // 头像 URL
    private String token; // 认证 Token
}
