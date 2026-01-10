package com.example.fitness.data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.fitness.data.model.entity.UserStats;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserStatsMapper extends BaseMapper<UserStats> {

    /**
     * 更新用户统计：分数累加
     */
    @Update("UPDATE user SET total_score = IFNULL(total_score, 0) + #{score}, updated_at = NOW() WHERE id = #{userId}")
    int incrementScore(@Param("userId") Long userId, @Param("score") Integer score);
}
