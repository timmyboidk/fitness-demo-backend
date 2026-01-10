package com.example.fitness.content.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户内容库实体类
 * 记录用户收藏或练过的动作、课程等。
 */
@Data
@TableName("user_library")
public class UserLibrary {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户唯一标识 */
    private Long userId;

    /** 内容 ID (如动作 ID) */
    private String itemId;

    /** 内容类型 (move | session) */
    private String itemType;

    /** 添加时间 */
    private LocalDateTime createdAt;
}
