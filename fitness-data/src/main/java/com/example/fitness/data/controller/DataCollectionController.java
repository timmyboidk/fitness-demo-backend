package com.example.fitness.data.controller;

import com.example.fitness.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/data")
public class DataCollectionController {

    @PostMapping("/collect")
    public Result<Void> collect(@RequestBody Map<String, Object> request) {
        log.info("Received collection request: {}", request);
        // This would typically also go through MQ
        return Result.success(null);
    }
}
