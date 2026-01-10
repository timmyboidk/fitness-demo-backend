package com.example.fitness.content.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 训练课程实体
 * 定义了一组有序的训练动作集合
 */
@Data
@TableName("training_session")
public class Session {
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 课程名称
     */
    private String name;

    /**
     * 难度等级 (novice, skilled, expert)
     */
    private String difficulty;

    /**
     * 预计时长 (分钟)
     */
    private Integer duration;

    /**
     * 封面图片 URL
     */
    private String coverUrl;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
