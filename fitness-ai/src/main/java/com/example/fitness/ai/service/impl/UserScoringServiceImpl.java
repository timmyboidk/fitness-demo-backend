package com.example.fitness.ai.service.impl;

import com.example.fitness.api.dto.ScoringRequest;
import com.example.fitness.api.dto.ScoringResponse;
import com.example.fitness.api.dto.ScoringResultEvent;
import com.example.fitness.ai.service.ScoringService;
import com.example.fitness.common.exception.BusinessException;
import com.example.fitness.common.result.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 用户运动评分服务实现类
 * 模拟 AI 评分算法生成分数，并将评分事件异步发送至消息队列。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserScoringServiceImpl implements ScoringService {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "frontend_event_stream";

    /**
     * 计算评分逻辑：基于余弦相似度的关键点比对
     */
    @Override
    @SuppressWarnings("unchecked")
    public ScoringResponse calculateScore(ScoringRequest request) {
        if (request.getMoveId() == null || request.getMoveId().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        // 1. 获取动作的标准模板数据 (模拟数据，实际应查库)
        double[] standardVector = getStandardTemplate(request.getMoveId());
        if (standardVector == null) {
            throw new BusinessException(ErrorCode.MODEL_NOT_FOUND);
        }

        // 2. 解析用户上传的关键点数据
        Map<String, Object> data = request.getData();
        if (data == null || !data.containsKey("keypoints")) {
            return ScoringResponse.builder().success(false).score(0).feedback(java.util.List.of("未检测到关键点数据")).build();
        }

        List<Map<String, Object>> keypoints = (List<Map<String, Object>>) data.get("keypoints");
        double[] userVector = normalizeKeypoints(keypoints);

        // 3. 计算余弦相似度并转换为 0-100 分数
        double similarity = calculateCosineSimilarity(standardVector, userVector);
        int score = Math.max(0, Math.min(100, (int) (similarity * 100)));

        // 4. 根据难度调整容差 (简单难度下分数略微上浮)
        // String difficulty = ... (可从 User 上下文获取，此处暂略)
        // if ("novice".equals(difficulty)) score = Math.min(100, score + 10);

        ScoringResponse response = ScoringResponse.builder()
                .success(true)
                .score(score)
                .feedback(generateFeedback(score))
                .build();

        // 5. 异步发送到 Kafka
        try {
            String userId = (String) data.getOrDefault("userId", "unknown");
            ScoringResultEvent event = ScoringResultEvent
                    .builder()
                    .userId(userId)
                    .moveId(request.getMoveId())
                    .score(score)
                    .timestamp(LocalDateTime.now())
                    .extraData(Collections.singletonMap("duration", 5)) // 模拟时长
                    .build();

            kafkaTemplate.send(TOPIC, Objects.requireNonNull(request.getMoveId()), event);
            log.info("已将评分事件发送至 Kafka, 动作 ID: {}, 分数: {}", request.getMoveId(), score);
        } catch (Exception e) {
            log.error("发送 Kafka 失败: {}", e.getMessage());
            // 非核心路径不中断
        }

        return response;
    }

    private double[] getStandardTemplate(String moveId) {
        // 简单模拟：假设标准化后的向量为全 1 或特定模式
        // 实际中应存储 17 个关键点 (x,y) 的归一化向量，共 34 维
        if ("m_squat".equals(moveId)) {
            double[] v = new double[34];
            java.util.Arrays.fill(v, 0.5);
            return v;
        }
        // 默认模板
        double[] v = new double[34];
        java.util.Arrays.fill(v, 0.5);
        return v;
    }

    private double[] normalizeKeypoints(List<Map<String, Object>> keypoints) {
        // 将关键点列表扁平化为向量 [x1, y1, x2, y2, ...]
        // 实际需做归一化处理（以髋部为原点，缩放尺度的）
        // 这里简化为直接提取
        double[] vector = new double[34];
        for (int i = 0; i < Math.min(keypoints.size(), 17); i++) {
            Map<String, Object> kp = keypoints.get(i);
            vector[i * 2] = Double.parseDouble(String.valueOf(kp.getOrDefault("x", 0)));
            vector[i * 2 + 1] = Double.parseDouble(String.valueOf(kp.getOrDefault("y", 0)));
        }
        return vector;
    }

    private double calculateCosineSimilarity(double[] v1, double[] v2) {
        if (v1.length != v2.length)
            return 0.0;
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < v1.length; i++) {
            dotProduct += v1[i] * v2[i];
            normA += Math.pow(v1[i], 2);
            normB += Math.pow(v2[i], 2);
        }
        if (normA == 0 || normB == 0)
            return 0.0;
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    private java.util.List<Object> generateFeedback(int score) {
        java.util.List<Object> feedback = new java.util.ArrayList<>();
        if (score < 60) {
            feedback.add("动作幅度不够，请尝试下蹲更深一点");
        } else if (score < 80) {
            feedback.add("动作基本标准，注意保持背部挺直");
        } else {
            feedback.add("完美！保持这个节奏");
        }
        return feedback;
    }
}
