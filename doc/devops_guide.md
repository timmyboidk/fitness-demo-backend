# DevOps & 部署指南 (DevOps Guide)

本指南旨在带领新入职开发者快速搭建开发环境，并指导运维团队在阿里云环境进行工业化部署。

---

## 1. 开发者快速入门 (Quick Start)

### 1.1 环境准备
*   **JDK 21**: 核心依赖。必须使用 JDK 21 以支持单机高并发的 `Virtual Threads`。
*   **Maven 3.9+**: 构建工具。
*   **Docker Desktop**: 用于在本地启动中间件。

### 1.2 本地一键启动
1. **启动容器化中间件**:
   在根目录下执行：
   ```bash
   docker-compose up -d mysql redis kafka zookeeper
   ```
2. **初始化数据库**:
   Docker 自动挂载了 `/sql/init.sql`。若未生效，请手动连接 `localhost:3306` 执行该脚本。
3. **构建并启动服务**:
   ```bash
   mvn clean install -DskipTests
   # 启动核心 API 模块
   mvn spring-boot:run -pl fitness-user
   ```

### 1.3 关键连接地址
*   **API 文档 (Swagger)**: `http://localhost:8080/swagger-ui/index.html`
*   **监控端点 (Actuator)**: `http://localhost:8080/actuator/health`

---

## 2. 模型交付与资源管理 (Model Delivery)

针对 AI 算法团队/供应商的交付标准：

### 2.1 交付路径
*   **存储位置**: 阿里云 OSS `oss://fitness-models-prod/vision/`。
*   **交付格式**: `pose_<move_id>_v<version>_<platform>.onnx`。
    - 示例: `pose_squat_v1.2.0_ios.onnx`。

### 2.2 发布流程
1.  **文件上传**: 将 `.onnx` 及对应的签名信息 (MD5) 上传至 OSS 特点目录。
2.  **CDN 刷新**: 刷新阿里云 CDN 预热，确保全球快速下载。
3.  **配置更新**: 修改数据库 `move` 表的 `model_url` 字段，客户端将在下一次启动时自动拉取。

---

## 3. 阿里云生产环境部署 (Enterprise Deployment)

### 3.1 基础设施选型
*   **Kubernetes (ACK)**: 部署后端微服务 Pods。
    - **HPA**: 设置 CPU/Memory 阈值 (65%) 自动扩缩容。
*   **云数据库 RDS (MySQL 8.0)**: 存储业务主数据。
*   **云分布式缓存 Redis**: 至少 2 主 2 从的集群模式。
*   **云消息队列 Kafka**: 订阅 `frontend_event_stream` 主题。

### 3.2 CI/CD 流水线 (阿里云云效)
1.  **单元测试**: 强制执行全量 `mvn test`，覆盖率需 > 80%。
2.  **构建镜像**: 使用 Oracle GraalVM 或标准 JDK 21 的 Alpine 镜像，减小体积。
3.  **发布策略**: 
    - 灰度发布：先更新 20% 的节点。
    - 自动分发：集成 SLS 日志追踪观察错误率。

### 3.3 监控与安全
*   **日志**: 开启 SLS 采集，配置关键字 `ERROR` 钉钉实时报警。
*   **安全**: 配置 WAF 拦截非法 API 探测。手机号字段在 `EncryptTypeHandler` 中完成 AES 加解密。

---

## 4. 故障排查 (Troubleshooting)

*   **Virtual Threads 阻塞**: 若系统负载过高，检查是否有传统第三方驱动（如老版本 JDBC）导致平台线程钉死。
*   **Kafka 消息积压**: 检查 `fitness-data` 消费者的 ACK 模式是否正确，并观察 `UserStatsMapper` 的写入压力。

---

## 5. 测试报告 (Test Report)

### 5.1 概览
已针对 `fitness-demo-backend` 完成代码修复与负载测试环境搭建。

### 5.2 修复内容
*   **配置**: 修复 JDK 21 兼容性及 Docker Redis 连接配置。
*   **代码**: 修复 `AbstractIntegrationTest` 资源泄露，增强 `UserService` 注册幂等性。
*   **数据**: 补全 Docker `init.sql` 缺失表结构 (`training_session`)。

### 5.3 负载测试结果 (Performance)
**场景**: 用户登录 -> 获取动作库 (k6 script)
**配置**: 5 VU 并发

| 指标                | 结果       | 说明                             |
| :------------------ | :--------- | :------------------------------- |
| **Login Success**   | **100%**   | 幂等性修复后，高并发注册无报错。 |
| **Library Success** | **~100%**  | 补全 schema 后接口返回正常。     |
| **Login Latency**   | **< 20ms** | 响应极快。                       |
| **Throughput**      | **~4 TPS** | 5 VU 下系统稳定。                |

### 5.4 关键发现
*   **幂等性**: 注册接口已通过 `DuplicateKeyException` 捕获实现幂等。
*   **数据一致性**: 必须保持 Docker `init.sql` 与 Testcontainers `schema.sql` 结构同步。
