package com.example.fitness.user.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体类
 *
 * <p>
 * 对应数据库 {@code user} 表，是系统核心实体。
 * 手机号字段通过 {@code EncryptTypeHandler} 进行 AES 加密存储。
 */
@Data
@TableName(value = "`user`")
public class User {

    /** 用户唯一标识，自增主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 手机号（AES 加密存储，通过 {@code EncryptTypeHandler} 自动加解密） */
    @com.baomidou.mybatisplus.annotation.TableField(typeHandler = com.example.fitness.common.handler.EncryptTypeHandler.class)
    private String phone;

    /** 用户昵称 */
    private String nickname;

    /** 密码（当前未使用，保留字段） */
    private String password;

    /** 微信 OpenID（唯一索引，用于微信登录关联） */
    private String openId;

    /** 微信小程序会话密钥（用于解密微信传输的敏感数据） */
    private String sessionKey;

    /** 运动难度等级（{@code novice} / {@code skilled} / {@code expert}） */
    private String difficultyLevel;

    /** 用户头像 URL */
    private String avatar;

    /** 累计训练得分（由 Kafka 消费者异步更新） */
    private Integer totalScore;

    /** 累计训练时长（秒，由 Kafka 消费者异步更新） */
    private Integer totalDuration;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
