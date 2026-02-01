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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 用户运动评分服务实现类
 * 
 * <p>
 * 基于余弦相似度算法对用户动作进行评分，
 * 并将评分结果异步发送至 Kafka 消息队列。
 * 
 * <p>
 * 评分流程：
 * <ol>
 * <li>获取标准动作模板向量</li>
 * <li>解析用户上传的关键点数据</li>
 * <li>计算余弦相似度并转换为 0-100 分数</li>
 * <li>生成反馈建议</li>
 * <li>异步发送评分事件到 Kafka</li>
 * </ol>
 * 
 * @author fitness-team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserScoringServiceImpl implements ScoringService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /** Kafka 主题名称 */
    private static final String TOPIC = "frontend_event_stream";

    /** 标准动作模板向量长度（17 个关键点 × 2 坐标） */
    private static final int VECTOR_LENGTH = 34;

    /** 最大关键点数量 */
    private static final int MAX_KEYPOINTS = 17;

    // ==================== 标准动作模板缓存 ====================

    private static final Map<String, double[]> STANDARD_TEMPLATE_CACHE;

    static {
        double[] defaultVector = new double[VECTOR_LENGTH];
        java.util.Arrays.fill(defaultVector, 0.5);
        STANDARD_TEMPLATE_CACHE = Map.of("m_squat", defaultVector);
    }

    // ==================== 核心评分方法 ====================

    /**
     * 计算用户动作评分
     */
    @Override
    @SuppressWarnings("unchecked")
    public ScoringResponse calculateScore(ScoringRequest request) {
        // 1. 参数校验
        validateRequest(request);

        // 2. 获取标准模板
        double[] standardVector = getStandardTemplate(request.getMoveId());

        // 3. 解析用户关键点
        Map<String, Object> data = request.getData();
        if (!hasKeypoints(data)) {
            return buildErrorResponse("未检测到关键点数据");
        }

        // 4. 计算评分
        List<Map<String, Object>> keypoints = (List<Map<String, Object>>) data.get("keypoints");
        int score = computeScore(standardVector, keypoints);

        // 5. 构建响应
        ScoringResponse response = buildSuccessResponse(score);

        // 6. 异步发送事件
        sendScoringEvent(request, data, score);

        return response;
    }

    // ==================== 私有辅助方法：参数校验 ====================

    /**
     * 校验评分请求参数
     */
    private void validateRequest(ScoringRequest request) {
        if (request.getMoveId() == null || request.getMoveId().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
    }

    /**
     * 检查是否包含关键点数据
     */
    private boolean hasKeypoints(Map<String, Object> data) {
        return data != null && data.containsKey("keypoints");
    }

    // ==================== 私有辅助方法：评分计算 ====================

    /**
     * 计算最终评分
     */
    private int computeScore(double[] standardVector, List<Map<String, Object>> keypoints) {
        double[] userVector = normalizeKeypoints(keypoints);
        double similarity = calculateCosineSimilarity(standardVector, userVector);
        return Math.max(0, Math.min(100, (int) (similarity * 100)));
    }

    /**
     * 获取标准动作模板向量
     */
    private double[] getStandardTemplate(String moveId) {
        double[] template = STANDARD_TEMPLATE_CACHE.get(moveId);
        if (template != null) {
            return template;
        }
        // 返回默认模板
        return STANDARD_TEMPLATE_CACHE.values().iterator().next();
    }

    /**
     * 将关键点列表归一化为向量
     * <p>
     * 格式：[x1, y1, x2, y2, ...]
     */
    private double[] normalizeKeypoints(List<Map<String, Object>> keypoints) {
        double[] vector = new double[VECTOR_LENGTH];
        int count = Math.min(keypoints.size(), MAX_KEYPOINTS);

        for (int i = 0; i < count; i++) {
            Map<String, Object> kp = keypoints.get(i);
            vector[i * 2] = parseCoordinate(kp, "x");
            vector[i * 2 + 1] = parseCoordinate(kp, "y");
        }

        return vector;
    }

    /**
     * 解析坐标值
     */
    private double parseCoordinate(Map<String, Object> keypoint, String key) {
        Object value = keypoint.getOrDefault(key, 0);
        return Double.parseDouble(String.valueOf(value));
    }

    /**
     * 计算两个向量的余弦相似度
     */
    private double calculateCosineSimilarity(double[] v1, double[] v2) {
        if (v1.length != v2.length) {
            return 0.0;
        }

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < v1.length; i++) {
            dotProduct += v1[i] * v2[i];
            normA += v1[i] * v1[i];
            normB += v2[i] * v2[i];
        }

        if (normA == 0 || normB == 0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    // ==================== 私有辅助方法：响应构建 ====================

    /**
     * 构建成功响应
     */
    private ScoringResponse buildSuccessResponse(int score) {
        return ScoringResponse.builder()
                .success(true)
                .score(score)
                .feedback(generateFeedback(score))
                .build();
    }

    /**
     * 构建错误响应
     */
    private ScoringResponse buildErrorResponse(String message) {
        return ScoringResponse.builder()
                .success(false)
                .score(0)
                .feedback(List.of(message))
                .build();
    }

    /**
     * 根据分数生成反馈建议
     */
    private List<Object> generateFeedback(int score) {
        List<Object> feedback = new ArrayList<>();

        if (score < 60) {
            feedback.add("动作幅度不够，请尝试下蹲更深一点");
        } else if (score < 80) {
            feedback.add("动作基本标准，注意保持背部挺直");
        } else {
            feedback.add("完美！保持这个节奏");
        }

        return feedback;
    }

    // ==================== 私有辅助方法：事件发送 ====================

    /**
     * 异步发送评分事件到 Kafka
     */
    private void sendScoringEvent(ScoringRequest request, Map<String, Object> data, int score) {
        try {
            ScoringResultEvent event = buildScoringEvent(request, data, score);
            kafkaTemplate.send(TOPIC, Objects.requireNonNull(request.getMoveId()), event);
            log.info("已将评分事件发送至 Kafka, 动作 ID: {}, 分数: {}", request.getMoveId(), score);
        } catch (Exception e) {
            // 非核心路径，仅记录日志
            log.error("发送 Kafka 失败: {}", e.getMessage());
        }
    }

    /**
     * 构建评分事件对象
     */
    private ScoringResultEvent buildScoringEvent(ScoringRequest request, Map<String, Object> data, int score) {
        String userId = (String) data.getOrDefault("userId", "unknown");

        return ScoringResultEvent.builder()
                .userId(userId)
                .moveId(request.getMoveId())
                .score(score)
                .timestamp(LocalDateTime.now())
                .extraData(Collections.singletonMap("duration", 5))
                .build();
    }
}
