package com.example.fitness.content.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 训练课程与动作关联实体
 */
@Data
@TableName("session_move_relation")
public class SessionMove {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long sessionId;
    private Long moveId;

    /**
     * 排序顺序
     */
    private Integer sortOrder;

    /**
     * 该动作在此课程中的持续时长或次数
     */
    private Integer durationSeconds;

    private LocalDateTime createdAt;
}
