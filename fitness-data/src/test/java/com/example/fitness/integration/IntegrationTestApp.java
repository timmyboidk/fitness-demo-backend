package com.example.fitness.integration;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import com.example.fitness.ControllerTestApp;

@SpringBootApplication
@ComponentScan(basePackages = "com.example.fitness", excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = ControllerTestApp.class))
@MapperScan("com.example.fitness.data.mapper")
public class IntegrationTestApp {
}
