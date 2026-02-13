package com.example.fitness.content.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 训练课程与动作关联实体
 *
 * <p>
 * 对应数据库 {@code session_move_relation} 多对多关联表，
 * 定义了课程中包含哪些动作以及排列顺序和持续时长。
 */
@Data
@TableName("session_move_relation")
public class SessionMove {

    /** 自增主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联的课程 ID（外键，关联 {@code training_session} 表） */
    private Long sessionId;

    /** 关联的动作 ID（外键，关联 {@code move} 表） */
    private Long moveId;

    /** 动作在课程中的排列顺序（升序排列） */
    private Integer sortOrder;

    /** 该动作在此课程中的执行时长（秒） */
    private Integer durationSeconds;

    /** 创建时间 */
    private LocalDateTime createdAt;
}
