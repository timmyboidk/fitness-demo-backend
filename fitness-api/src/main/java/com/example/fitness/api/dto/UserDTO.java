package com.example.fitness.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户信息 DTO
 *
 * <p>
 * 用于向客户端返回用户基本资料及认证令牌。
 * 在登录响应中 {@code token} 字段有值；在个人资料查询中 {@code token} 为 {@code null}。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    /** 用户唯一标识（数据库自增 ID 转字符串） */
    private String id;

    /** 用户昵称 */
    private String nickname;

    /** 用户手机号（脱敏显示：仅前三后四位可见） */
    private String phone;

    /** 用户头像 URL */
    private String avatar;

    /** JWT 认证 Token（仅登录时返回，查询场景为 {@code null}） */
    private String token;
}
