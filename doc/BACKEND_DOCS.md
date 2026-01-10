# 健身后端文档

## 1. 概览
这是一个设计用于高并发健身评分和数据收集的 **模块化单体 (Modular Monolith)** Spring Boot 应用程序。

**模块:**
*   `fitness-common`: 核心工具类与拦截器。
*   `fitness-user`: 用户认证、微信登录及首次设置流程 (Onboarding)。
*   `fitness-content`: 动作内容管理系统 (CMS)。
*   `fitness-ai`: AI 动作评分逻辑与 Kafka 事件流集成。
*   `fitness-data`: 异步数据收集与分析。

## 2. 部署指南

### 先决条件
*   JDK 17
*   Maven 3.8+
*   Docker & Docker Compose

### 基础设施启动 (Docker)
在运行应用程序之前，请启动必要的中间件：
```bash
docker-compose up -d mysql redis kafka zookeeper
```
> [!NOTE]
> 默认 Kafka 配置已调整，本地开发请连接 `localhost:29092`。

### 本地开发运行
1. **构建并安装模块**:
   由于是多模块项目，必须先在根目录运行安装：
   ```bash
   mvn clean install -DskipTests
   ```

2. **启动应用 (以 fitness-user 为入口)**:
   使用 Maven 启动，并覆盖必要的连接参数以适配本地环境：
   ```bash
   mvn spring-boot:run -pl fitness-user -Dspring-boot.run.arguments=" \
     --spring.datasource.url=jdbc:mysql://localhost:3306/fitness_db?useUnicode=true&characterEncoding=utf-8&useSSL=false&allowPublicKeyRetrieval=true \
     --spring.data.redis.host=localhost \
     --spring.kafka.bootstrap-servers=localhost:29092"
   ```

## 3. 技术规范与实践

### 消息队列 (Kafka)
*   **序列化**: 项目使用 `JsonSerializer`。确保所有通过 `KafkaTemplate` 发送的 DTO 都具备无参构造函数。
*   **配置**: 消费端需配置 `spring.json.trusted.packages: "*"` 以支持跨模块的数据包。

### 数据库管理
*   **Redis**: 使用 `spring.data.redis` 属性命名规范取代已弃用的 `spring.redis`。
*   **MySQL**: 初始表结构位于 `sql/init.sql`。

## 4. 故障排除

*   **Kafka 序列化异常**: 
    - 现象：`Can't convert value of class ... to class ... StringSerializer`
    - 解决：在 `application.yml` 中明确指定 `value-serializer: org.springframework.kafka.support.serializer.JsonSerializer`。
*   **数据库连接失败**: 
    - 检查 Docker 容器状态：`docker ps`。
    - 验证 `SPRING_DATASOURCE_URL` 中的主机名在容器内外是否正确（本地开发用 `localhost`，容器内用 `mysql`）。
*   **Kafka 端口冲突**:
    - 本地主机应通过 `29092` 端口访问，容器内部通信使用 `9092`。

## 5. API 参考

| 功能         | 方法   | 路径                   | 关键 Payload                                               |
| :----------- | :----- | :--------------------- | :--------------------------------------------------------- |
| 手机登录     | `POST` | `/api/auth`            | `{ "type": "login_phone", "payload": { "phone": "..." } }` |
| 入职设置     | `POST` | `/api/auth/onboarding` | `{"userId": "1", "difficultyLevel": "expert"}`             |
| 获取动作库   | `GET`  | `/api/library`         | `?difficultyLevel=novice`                                  |
| AI 评分      | `POST` | `/api/ai/score`        | `{"moveId": "m_squat", "data": { ... } }`                  |
| 批量数据收集 | `POST` | `/api/data/collect`    | `{"sessionId": "...", "items": [...]}`                     |

## 6. 可观测性 (Observability)

### API 文档 (Swagger/OpenAPI)
*   **地址**: `http://localhost:8080/swagger-ui.html`
*   **用途**: 在线调试接口，查看参数定义。

### 监控指标 (Actuator)
*   **健康检查**: `/actuator/health`
*   **应用信息**: `/actuator/info`
*   **指标数据**: `/actuator/metrics`

## 7. 未来路线图 (Roadmap)
*   **数据库迁移**: 集成 Flyway 管理数据库版本变更。
*   **链路追踪**: 引入 Micrometer Tracing + Zipkin 实现全链路监控。
*   **容器化**: 提供 K8s 部署清单 (Helm Charts)。
