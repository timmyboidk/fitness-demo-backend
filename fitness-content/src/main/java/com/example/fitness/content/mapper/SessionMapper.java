package com.example.fitness.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.fitness.content.model.entity.Session;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SessionMapper extends BaseMapper<Session> {
}
