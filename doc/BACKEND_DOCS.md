# 健身后端文档

## 1. 概览
这是一个设计用于高并发健身评分和数据收集的 **模块化单体 (Modular Monolith)** Spring Boot 应用程序。

**模块:**
*   `fitness-common`: 核心工具类。
*   `fitness-user`: 认证与入职流程。
*   `fitness-content`: 动作内容管理系统 (CMS)。
*   `fitness-ai`: AI 评分与事件流处理。
*   `fitness-data`: 分析与数据仓库摄取。

## 2. 部署指南

### 先决条件
*   JDK 17+ (项目已降级为 Java 17)
*   Maven 3.8+
*   Docker & Kubernetes (可选)

### 本地开发
```bash
# 构建
./mvnw clean install

# 运行 (主入口点通常在 fitness-user 逻辑中，或使用专用运行器)
# 理想情况下运行生成的 jar 包
java -jar fitness-user/target/fitness-user-0.0.1-SNAPSHOT.jar
```

### Docker 构建
在根目录创建 `Dockerfile`:
```dockerfile
FROM eclipse-temurin:17-jre-alpine
COPY fitness-user/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### Kubernetes (K8s) 部署
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: fitness-backend
spec:
  replicas: 2
  selector:
    matchLabels:
      app: fitness-backend
  template:
    metadata:
      labels:
        app: fitness-backend
    spec:
      containers:
      - name: fitness-backend
        image: your-registry/fitness-backend:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
```

## 3. 运维指南

### 日志
*   默认输出到 STDOUT (生产环境建议使用 JSON 格式)。
*   建议集成 **阿里云 SLS** 或 **ELK Stack**。

### 监控
*   暴露 Actuator 端点 (`/actuator/prometheus`) 用于 Prometheus 抓取。
*   关键指标: `http_server_requests_seconds`, `jvm_memory_used_bytes`。

### 故障排除
*   **AI 评分缓慢:** 检查 `frontend_event_stream` 主题的 Kafka 积压情况。
*   **OOM (内存溢出):** 增加堆大小或检查 `DataCollectionConsumer` 是否存在内存泄漏。

## 4. API 参考

### 认证 (Auth)
*   `POST /api/auth`: 登录 (手机/微信)。
    *   Payload: `{ "type": "login_phone", "payload": { "phone": "..." } }`
*   `POST /api/auth/onboarding`: 设置难度。

### 动作库 (Library)
*   `GET /api/library?difficulty=novice`: 列出动作。

### AI 与 数据 (AI & Data)
*   `POST /api/ai/score`: 同步评分。
*   `POST /api/data/collect`: 异步批量数据收集。
    *   Payload: `{ "sessionId": "...", "items": [...] }`
