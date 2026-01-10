package com.example.fitness.data.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user")
public class UserStats {
    @TableId(type = IdType.AUTO)
    private Long id;

    // 假设数据库有这些字段，如无则需 Flyway 添加
    private Integer totalScore;
    private Integer totalDuration;
    private LocalDateTime updatedAt;
}
