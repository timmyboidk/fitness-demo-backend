package com.example.fitness.content.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("move")
public class Move {
    @TableId(type = IdType.INPUT)
    private String id;
    private String name;
    private String difficulty; // novice | skilled | expert
    private String modelUrl;
    private String scoringConfigJson; // Stored as JSON string
    private LocalDateTime createdAt;
}
