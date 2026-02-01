package com.example.fitness.data.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户训练统计实体类
 * 
 * <p>
 * 存储用户的累计训练数据，包括：
 * <ul>
 * <li>累计训练得分</li>
 * <li>累计训练时长（秒）</li>
 * <li>最后更新时间</li>
 * </ul>
 * 
 * <p>
 * 该实体映射到 user 表的统计相关字段，用于排行榜和用户成就等功能。
 * 
 * @author fitness-team
 * @since 1.0.0
 */
@Data
@TableName("user")
public class UserStats {

    /**
     * 用户唯一标识，自增主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 累计训练得分
     * <p>
     * 用户所有训练课程获得的分数总和
     */
    private Integer totalScore;

    /**
     * 累计训练时长（秒）
     * <p>
     * 用户所有训练课程花费的时间总和
     */
    private Integer totalDuration;

    /**
     * 最后更新时间
     * <p>
     * 统计数据最近一次更新的时间戳
     */
    private LocalDateTime updatedAt;
}
