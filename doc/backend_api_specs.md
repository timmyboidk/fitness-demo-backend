# 后端架构与 API 规范 (Backend Architecture & API Specification)

## 1. 架构设计 (Technial Architecture)

**设计理念:** 采用 "模块化单体 (Modular Monolith)" 作为起步架构，预留微服务拆分接口。
*理由:* 团队初期需快速迭代，微服务会引入过高的运维复杂度（服务发现、分布式事务）。模块化单体保持了代码边界清晰，当单模块（如 AI Scoring）负载过高时，可零成本拆分为独立服务。

### 1.1 技术栈选型 (Tech Stack)
| 领域          | 选型                                         | 这为什么要选它?                                                                   |
| :------------ | :------------------------------------------- | :-------------------------------------------------------------------------------- |
| **Framework** | **Spring Boot 3.2+ / 4.x** (Java 21 LTS)     | 虚拟线程 (Virtual Threads) 支持高并发，生态最成熟。                               |
| **ORM**       | **MyBatis-Plus**                             | 在国内开发环境中最通用，内置分页插件和代码生成器，极大提升 CRUD 效率。            |
| **Database**  | **MySQL 8.0** (Tx) + **Apache Doris** (OLAP) | 事务处理与实时分析分离。Doris 兼容 MySQL 协议，运维成本远低于 Hadoop/ClickHouse。 |
| **Cache**     | **Redis 7.0**                                | 使用 Redis Cluster 模式，处理 Session、限流令牌桶、排行榜。                       |
| **MQ**        | **Kafka** (Kraft Mode)                       | 去除 Zookeeper 更轻量。用于异步削峰（埋点日志、支付回调）。                       |
| **Deploy**    | **Docker** + **K8s** (阿里云 ACK)            | 虽然是单体，但容器化部署为扩容和容灾打基础。                                      |

### 1.2 模块划分 (Module Design)
项目结构：`fitness-backend` (Maven Multi-module)
*   `fitness-common`: 公共工具 (Utils, Constants), 统一异常处理, 埋点 SDK。
*   `fitness-api`: 定义 Feign Client 接口和 DTO，供模块间调用。
*   `fitness-user` (Module): 用户、认证、社交关系 (WeChat)。
*   `fitness-content` (Module): 动作库、课程、CMS。
*   `fitness-ai` (Module): AI 评分接收、数据清洗、与 Doris 交互。
*   `fitness-pay` (Module): 支付网关 (WeChat Pay/Alipay) 抽象层。

---

## 2. 运维与部署实战 (DevOps & Deployment)

### 2.1 部署架构
*   **开发环境 (Dev):** 单机 Docker Compose (MySQL + Redis + App)。
*   **生产环境 (Prod):** 阿里云 ACK (Kubernetes)。
    *   **Ingress:** Nginx Ingress Controller (SSL 卸载, 限流)。
    *   **Pod 策略:** 至少 2 Replicas，配置 HPA (CPU > 60% 自动扩容)。

### 2.2 容灾方案 (Disaster Recovery)
1.  **数据库容灾:** MySQL 开启 MGR (Group Replication) 或阿里云 RDS 高可用版 (一主一备，自动切换)。
2.  **多可用区 (Multi-AZ):** K8s 节点分布在 `cn-hangzhou-g` 和 `cn-hangzhou-h`，防止单机房故障。
3.  **降级策略:** 当 `Apache Doris` 不可用时，AI 评分接口仅返回成功但不写入历史，保证 App 主流程不崩。

### 2.3 监控体系
*   **Logs:** 阿里云 SLS (Log Service) 采集 Console 日志。
*   **Metrics:** Prometheus (采集 JVM/Tomcat 指标) + Grafana (Dashboard)。
*   **Trace:** SkyWalking (链路追踪)，定位慢 SQL 和微服务调用延迟。

---

## 3. 关键业务模块设计 (Core Modules)

### 3.1 社交整合 (Social Integration)
*   **微信登录流程:**
    1.  App 端调用微信 SDK 获取 `code`。
    2.  调用后端 `POST /api/auth { type: "login_wechat" }`。
    3.  后端通过 `WxMaService` (WxJava SDK) 换取 `openid` 和 `session_key`。
    4.  若 `openid` 不存在，自动注册；若存在，颁发 JWT。

### 3.2 支付系统 (Payment - Planned)
*(预留设计，暂不实现)*
*   **接口抽象:** 定义 `PaymentStrategy` 接口 (`pay()`, `refund()`, `query()`)。
*   **支付回调:** 必须通过 MQ (Kafka) 处理回调，防止微信服务器重试导致的数据不一致。
*   **幂等性:** 订单号作为唯一键，Redis `setnx` 锁防止并发支付。

---

## 4. 数据基础设施 (Data Infrastructure)

### 4.1 消息队列设计 (Kafka Topics)
使用 Kafka 作为高吞吐缓冲层，削峰填谷。

*   **Topic:** `frontend_event_stream`
*   **Partitions:** 24 (按 `sessionId` Hash 分区，保证同一会话有序)
*   **Replication:** 3
*   **Payload Schema:**
```json
{
  "traceId": "uuid-v4",
  "clientTime": 1700000000000,
  "clientIp": "1.2.3.4",
  "items": [ ... ] // 原始批量上传数组
}
```

### 4.2 实时数仓模型 (Apache Doris)
数据经 Flink 清洗后落入 Doris，支持实时看板。

**1. 动作评分明细表 (`dwd_action_score`)**
Unique Key 模型，按天分区。
```sql
CREATE TABLE dwd_action_score (
    event_time      DATETIME NOT NULL,
    user_id         BIGINT,
    session_id      VARCHAR(64),
    move_id         VARCHAR(32),
    score           INT,
    feedback_type   VARCHAR(32),
    device_model    VARCHAR(64),
    -- 均值聚合字段
    accuracy        DOUBLE SUM,
    latency_ms      INT MAX
)
UNIQUE KEY(event_time, user_id, session_id, move_id)
PARTITION BY RANGE(event_time) ()
DISTRIBUTED BY HASH(user_id) BUCKETS 16;
```

### 4.3 冷存储规范 (OSS)
用于存储 3D 训练所需的二进制大文件（由 Flink 剥离写入）。

*   **Bucket:** `fitness-training-raw`
*   **Path:** `date={YYYY-MM-DD}/model_ver={v1.2}/{move_id}/{session_id}.bin`
*   **Format:** Protobuf 序列化的关键点序列 + 原始 MVP 矩阵。
*   **Lifecycle:** 30天后自动转归档存储 (Archive)。


---

## 3. 认证服务 (`/api/auth`)

> **Base URL:** `/api` (Dev) or `https://api.yourdomain.com` (Prod)

**端点:** `POST /api/auth`

### 3.1 手机验证码登录/注册
**请求体:**
```json
{
  "type": "login_phone",
  "payload": {
    "phone": "13800138000",
    "code": "1234"
  }
}
```

**响应:**
```json
{
  "success": true,
  "user": {
    "_id": "u_123",
    "nickname": "User 8000",
    "phone": "13800138000",
    "token": "eyJhbG..." 
  }
}
```

### 3.2 微信登录
**请求体:**
```json
{
  "type": "login_wechat",
  "payload": { "code": "wx_code_xyz" }
}
```

**响应:**
```json
{
  "success": true,
  "user": {
    "_id": "u_wx_123",
    "nickname": "WeChat User",
    "avatar": "http://..."
  }
}
```

---

### 3.3 用户初始引导 (Onboarding)
**端点:** `POST /api/auth/onboarding`
**请求体:**
```json
{
  "userId": "u_123",
  "difficultyLevel": "novice" // novice | skilled | expert
}
```
**响应:**
```json
{
  "success": true,
  "config": {
    "scoringTolerance": 20, // 判定公差 (度)
    "recommendedPlan": "plan_starter"
  }
}
```

---

## 4. 库管理 (`/api/library`)

### 4.1 获取内容库 (按难度分发)
后端根据用户 Profile 的 `difficultyLevel` 自动筛选返回的动作标准。
**端点:** `GET /api/library`
**响应:**
```json
{
  "moves": [
    { 
      "id": "m_squat", 
      "name": "Squat",
      "modelUrl": "https://oss.../squat_novice.onnx", // 对应难度的标准模型
      "scoringConfig": {
        "angleThreshold": 20, // 新手 20度，专家 5度
        "holdTime": 2
      }
    }
  ],
  "sessions": [...]
}
```

### 4.2 添加项目到用户库
**端点:** `POST /api/library`
**请求体:**
```json
{
  "type": "add_item",
  "payload": { "userId": "u_123", "itemId": "m_squat", "itemType": "move" }
}
```

---

## 5. 核心业务 API (`/api/core`)

### 5.1 动作评分 (AI Scoring)
**端点:** `POST /api/ai/score`
**请求体:**
```json
{ "moveId": "m_01", "data": { "keypoints": [...] } }
```
**响应:** `{ "success": true, "score": 85, "feedback": [...] }`

### 5.2 用户统计同步
**端点:** `POST /api/user/stats`
**请求体:**
```json
{ "userId": "u_123", "stats": { "totalWorkouts": 10 } }
```

### 5.3 模型分发与管理 (Model Management - OTA)
负责管理 `.onnx` 模型的版本控制与热更新检查。

**端点:** `GET /api/core/models/latest`

**请求参数:**
*   `platform`: `ios` | `android`
*   `currentVersion`:String (e.g., "1.0.0")

**响应:**
```json
{
  "hasUpdate": true,
  "data": {
    "version": "1.1.0",
    "downloadUrl": "https://oss.aliyun.com/models/pose_v1.1_ios.onnx",
    "md5": "a3f8...",
    "forceUpdate": false,
    "releaseNotes": "Optimized for iPhone 15 NPU."
  }
}
```

---

## 6. 数据采集 (`/api/data`)


此接口由 Kafka 消费者异步处理，保证高吞吐。

### 6.1 上传埋点日志
**端点:** `POST /api/data/collect`
**请求体:**
```json
{
  "sessionId": "s_789",
  "items": [
    {
      "moveId": "m_squat",
      "score": 45,
      "feedbackType": "correction", 
      "flags": { "isUserCorrection": true },
      // 仅在符合 privacy policy 的 Wi-Fi 环境下上传 keypoints
      "keypointsSnapshot": { ... } 
    }
  ]
}
```
**响应:** `{ "success": true }`
