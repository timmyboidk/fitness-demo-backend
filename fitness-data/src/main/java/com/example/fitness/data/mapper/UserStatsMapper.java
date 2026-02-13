package com.example.fitness.data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.fitness.data.model.entity.UserStats;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 用户训练统计 Mapper 接口
 *
 * <p>
 * 继承 MyBatis-Plus {@link BaseMapper}，提供对 {@code user} 表中
 * 统计字段（{@code total_score}, {@code total_duration}）的原子性更新操作。
 *
 * <p>
 * 主要由 {@code DataCollectionConsumer} 在消费 Kafka 评分事件后调用。
 *
 * @see com.example.fitness.data.model.entity.UserStats
 */
@Mapper
public interface UserStatsMapper extends BaseMapper<UserStats> {

    /**
     * 原子性累加用户训练得分
     *
     * <p>
     * 使用 {@code IFNULL} 兼容 {@code total_score} 为 {@code NULL} 的情况，
     * 同时更新 {@code updated_at} 为当前时间。
     *
     * @param userId 用户 ID
     * @param score  本次需累加的分数（正整数）
     * @return 受影响的行数（正常应为 1）
     */
    @Update("UPDATE user SET total_score = IFNULL(total_score, 0) + #{score}, updated_at = NOW() WHERE id = #{userId}")
    int incrementScore(@Param("userId") Long userId, @Param("score") Integer score);
}
