# 后端架构与 API 规范 (Backend Architecture & API Specification)

## 1. 架构设计 (Technical Architecture)

**设计理念:** 采用 "模块化单体 (Modular Monolith)" 作为起步架构，预留微服务拆分接口。
*理由:* 团队初期需快速迭代，微服务会引入过高的运维复杂度（服务发现、分布式事务）。模块化单体保持了代码边界清晰，当单模块（如 AI Scoring）负载过高时，可零成本拆分为独立服务。

### 1.1 技术栈选型 (Tech Stack)
| 领域          | 选型                                         | 为什么要选它?                                                                     |
| :------------ | :------------------------------------------- | :-------------------------------------------------------------------------------- |
| **Framework** | **Spring Boot 3.2+ / 4.x** (Java 21 LTS)     | 虚拟线程 (Virtual Threads) 支持高并发，生态最成熟。                               |
| **ORM**       | **MyBatis-Plus**                             | 在国内开发环境中最通用，内置分页插件和代码生成器，极大提升 CRUD 效率。            |
| **Database**  | **MySQL 8.0** (Tx) + **Apache Doris** (OLAP) | 事务处理与实时分析分离。Doris 兼容 MySQL 协议，运维成本远低于 Hadoop/ClickHouse。 |
| **Cache**     | **Redis 7.0**                                | 使用 Redis Cluster 模式，处理 Session、限流令牌桶、排行榜。                       |
| **MQ**        | **Kafka** (Kraft Mode)                       | 去除 Zookeeper 更轻量。用于异步削峰（埋点日志、支付回调）。                       |
| **Deploy**    | **Docker** + **K8s** (阿里云 ACK)            | 虽然是单体，但容器化部署为扩容和容灾打基础。                                      |

### 1.2 模块划分 (Module Design)
项目结构：`fitness-demo-backend` (Maven Multi-module)
*   `fitness-common`: 公共工具 (Utils, Constants), 统一异常处理, 数据库敏感字段加密 (AES)。
*   `fitness-api`: 定义 DTO 和通用响应格式。
*   `fitness-user`: 用户管理、身份认证、社交关系 (WeChat)。
*   `fitness-content`: 动作库 (Moves)、训练计划 (Sessions)、用户收藏夹。
*   `fitness-ai`: AI 动作评分逻辑抽象与 Doris 交互。
*   `fitness-data`: 高吞吐埋点数据采集，包含 Kafka 生产者与降级日志。

---

## 2. 运维与部署实战 (DevOps & Deployment)

### 2.1 部署架构
*   **开发环境 (Dev):** 单机 Docker Compose (MySQL + Redis + App)。
*   **生产环境 (Prod):** 阿里云 ACK (Kubernetes)。
    *   **Ingress:** Nginx Ingress Controller (SSL 卸载, 限流)。
    *   **Pod 策略:** 至少 2 Replicas，配置 HPA (CPU > 60% 自动扩容)。

### 2.2 容灾方案 (Disaster Recovery)
1.  **数据库容灾:** MySQL 开启 MGR (Group Replication) 或阿里云 RDS 高可用版 (一主一备，自动切换)。
2.  **多可用区 (Multi-AZ):** K8s 节点分布在多个可用区，防止单机房故障。
3.  **降级策略:** 当 `Kafka` 不可用时，数据采集接口会自动切换为本地磁盘日志记录，保证业务不中断。

### 2.3 监控体系
*   **Logs:** 阿里云 SLS (Log Service) 采集 Console 日志。
*   **Metrics:** Prometheus (采集 JVM/Tomcat 指标) + Grafana (Dashboard)。
*   **Trace:** Spring Cloud Sleuth / SkyWalking 链路追踪。

---

## 3. 业务逻辑与数据流设计 (Service Logic & Data Flow)

### 3.1 核心业务流量
1. **用户身份流**:
   - 用户通过手机号/微信登录 -> `fitness-user` 校验通过 -> 颁发 JWT。
   - 后续所有请求通过 `Authorization` Header 携带 Token。
2. **训练内容分发**:
   - App 根据用户 `difficulty_level` 请求 `fitness-content`。
   - 后端下发对应的 ONNX 模型路径及评分参数 (Angle Tolerance)。
3. **实时评分与反馈**:
   - 客户端 Pose Detection 识别关键点 -> 调用 `fitness-ai` 或本地运行模型 -> 生成即时反馈。
4. **数据采集流**:
   - 训练数据异步上传至 `fitness-data` -> 写入 Kafka -> 实时数仓 (Doris) 分析 -> 返回用户统计报表。

### 3.2 数据库模式设计 (Database Schema)

#### `user` (用户主表 - MySQL)
| 字段             | 类型        | 说明                   |
| :--------------- | :---------- | :--------------------- |
| id               | BIGINT (PK) | 用户唯一 ID (自增)     |
| phone            | VARCHAR     | 手机号 (AES 加密存储)  |
| nickname         | VARCHAR     | 用户昵称               |
| difficulty_level | INT         | 1:新手, 2:进阶, 3:专家 |
| total_score      | BIGINT      | 累计训练积分           |
| total_duration   | BIGINT      | 累计训练时长 (秒)      |

#### `move` (动作定义表 - MySQL)
| 字段           | 类型         | 说明                         |
| :------------- | :----------- | :--------------------------- |
| id             | VARCHAR (PK) | 动作代码 (如 squat)          |
| name           | VARCHAR      | 动作展示名称                 |
| model_url      | VARCHAR      | ONNX 模型文件 URL            |
| scoring_config | JSON         | 包含角度阀值、判定帧率等配置 |

#### `user_library` (用户内容库 - MySQL)
| 字段      | 类型    | 说明           |
| :-------- | :------ | :------------- |
| user_id   | BIGINT  | 用户 ID        |
| item_id   | VARCHAR | 动作或方案 ID  |
| item_type | VARCHAR | move / session |

---

## 4. 关键业务模块细节 (Core Modules)

### 4.1 社交整合 (Social Integration)
*   **微信登录流程:**
    1.  App 端调用微信 SDK 获取 `code`。
    2.  调用后端 `POST /api/auth { type: "login_wechat" }`。
    3.  后端通过 `WxMaService` (WxJava SDK) 换取 `openid` 和 `session_key`。
    4.  若 `openid` 不存在，自动注册；若存在，颁发 JWT。

### 4.2 数据基础设施 (Data Infrastructure)
使用 Kafka 作为高吞吐缓冲层，削峰填谷。
*   **Topic:** `frontend_event_stream`
*   **Partitions:** 24 (按 `sessionId` Hash 分区)
*   **降级机制**: 开发者在 `DataCollectionController` 中集成了 CircuitBreaker。当 Kafka 响应过慢或连接失败，数据将直接落盘到 `/tmp/telemetry-fallback.log`。

---

## 5. 认证服务 API (`/api/auth`)

**端点:** `POST /api/auth`

### 5.1 手机验证码登录/注册
**请求体:**
```json
{
  "type": "login_phone",
  "phone": "13800138000",
  "code": "1234"
}
```

### 5.2 微信登录
**请求体:**
```json
{
  "type": "login_wechat",
  "code": "wx_code_xyz"
}
```

### 5.3 用户初始引导 (Onboarding)
**端点:** `POST /api/auth/onboarding`
**请求体:**
```json
{
  "userId": "123",
  "difficultyLevel": "novice"
}
```

---

## 6. 内容与统计 API

### 6.1 获取训练库
**端点:** `GET /api/library`
**说明:** 后端根据登录用户的 Profile 自动返回适配难度的模型地址。

### 6.2 实时评分数据采集
**端点:** `POST /api/data/collect`
**请求体:**
```json
{
  "sessionId": "s_789",
  "items": [
    {
      "moveId": "m_squat",
      "score": 85,
      "feedbackType": "correct"
    }
  ]
}
```

### 6.3 用户统计同步
**端点:** `POST /api/user/stats`
**请求体:**
```json
{
  "userId": "123",
  "stats": { "totalWorkouts": 10 }
}
```
