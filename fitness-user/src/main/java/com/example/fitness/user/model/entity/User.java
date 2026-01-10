package com.example.fitness.user.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体类 - 对应数据库 user 表
 */
@Data
@TableName(value = "user", autoResultMap = true)
public class User {
    @TableId(type = IdType.AUTO)
    private Long id; // 用户 ID

    @com.baomidou.mybatisplus.annotation.TableField(typeHandler = com.example.fitness.common.handler.EncryptTypeHandler.class)
    private String phone; // 手机号 (加密存储)

    private String nickname; // 昵称
    private String password; // 密码
    private String openId; // 微信 OpenID
    private String sessionKey; // 微信会话密钥
    private String difficultyLevel; // 运动难度等级
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
