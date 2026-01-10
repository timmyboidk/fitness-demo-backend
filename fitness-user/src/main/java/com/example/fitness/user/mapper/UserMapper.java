package com.example.fitness.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.fitness.user.model.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Mapper 接口
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
