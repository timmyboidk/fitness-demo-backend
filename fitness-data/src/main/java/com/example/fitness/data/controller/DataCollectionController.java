package com.example.fitness.data.controller;

import com.example.fitness.common.exception.BusinessException;
import com.example.fitness.common.result.ErrorCode;
import com.example.fitness.common.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 数据收集控制器 - 接收各类前端埋点及事件并推送到 Kafka
 */
@Slf4j
@RestController
@RequestMapping("/api/data")
@RequiredArgsConstructor
public class DataCollectionController {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "frontend_event_stream";

    /**
     * 批量数据收集接口
     * 
     * @param request 包含会话ID和事件列表数据
     */
    private final org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory circuitBreakerFactory;

    /**
     * 批量数据收集接口
     * 
     * @param request 包含会话ID和事件列表数据
     */
    @PostMapping("/collect")
    public Result<Void> collect(@RequestBody Map<String, Object> request) {
        if (request == null || request.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        log.info("收到数据收集请求: {}", request);

        // 使用 Circuit Breaker 保护 Kafka 发送
        return circuitBreakerFactory.create("kafkaSender").run(() -> {
            try {
                kafkaTemplate.send(TOPIC, request);
                log.info("已成功将数据收集事件发送至 Kafka");
                return Result.success(null);
            } catch (Exception e) {
                log.error("发送数据收集事件至 Kafka 失败: {}", e.getMessage());
                throw new BusinessException(ErrorCode.KAFKA_SEND_ERROR);
            }
        }, throwable -> fallback(request, throwable));
    }

    /**
     * 降级策略：本地日志记录，防止数据完全丢失
     */
    private Result<Void> fallback(Map<String, Object> request, Throwable t) {
        log.warn("Kafka 服务不可用，执行降级策略。数据写入本地日志: {}, 异常: {}", request, t.getMessage());
        // 在实际生产中，这里可以写入本地文件或 Sidecar 代理
        return Result.success(null); // 返回成功以避免前端重试风暴
    }
}
