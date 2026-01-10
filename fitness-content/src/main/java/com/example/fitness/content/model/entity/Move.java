package com.example.fitness.content.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 健身动作实体类 - 对应数据库 move 表
 */
@Data
@TableName("move")
public class Move {
    @TableId(type = IdType.INPUT)
    private String id; // 动作唯一标识 (如 m_squat)
    private String name; // 动作名称
    private String difficulty; // 难度等级 (novice | skilled | expert)
    private String modelUrl; // AI 模型下载地址
    private String scoringConfigJson; // 评分配置 (存储为 JSON 字符串)
    private LocalDateTime createdAt;
}
