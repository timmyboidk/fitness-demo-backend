# 后端架构与 API 规范 (Backend Architecture & API Specification)

## 1. 架构设计 (Technical Architecture)

### 1.1 设计理念
采用 **模块化单体 (Modular Monolith)** 架构。通过清晰的 Maven 模块划分（`fitness-user`, `fitness-content`, `fitness-ai`, `fitness-data`, `fitness-pay`），实现在单一代码仓库中保持高内聚低耦合。未来可根据业务负载（如 AI 模块）轻松剥离为独立微服务。

### 1.2 技术栈选型 (Tech Stack)
| 领域                | 选型              | 版本   | 说明                                                              |
| :------------------ | :---------------- | :----- | :---------------------------------------------------------------- |
| **语言**            | **Java 21 (LTS)** | 21     | 核心特性：虚拟线程 (Virtual Threads) 实现海量数据采集的高并发处理 |
| **框架**            | **Spring Boot**   | 3.5.x  | 现代化的应用框架，内置可观测性支持                                |
| **数据访问**        | **MyBatis-Plus**  | 3.5.9  | 极速开发 CRUD，支持 Lambda 表达式                                 |
| **关系型数据库**    | **MySQL**         | 8.0    | 用于核心业务事务 (Tx) 存储                                        |
| **时序/分析数据库** | **Apache Doris**  | Latest | 经由 Kafka 消费录入，用于大规模埋点数据实时分析                   |
| **分布式缓存**      | **Redis**         | 7.0    | Session 管理、限流（令牌桶）、用户信息热点缓存                    |
| **消息队列**        | **Kafka**         | 7.5.0  | 采用 Kraft 模式。用于削峰填谷，连接数据采集与分析层               |
| **部署容载**        | **Docker + K8s**  | -      | 生产环境基于阿里云 ACK                                            |

---

## 2. 数据库设计 (Database Schema)

### 2.1 核心表结构

#### 用户表 (`user`)
| 字段             | 类型        | 描述                  |
| :--------------- | :---------- | :-------------------- |
| id               | BIGINT (PK) | 用户唯一 ID (自增)    |
| phone            | VARCHAR(32) | 手机号 (AES 加密存储) |
| nickname         | VARCHAR(64) | 用户昵称              |
| open_id          | VARCHAR(64) | 微信 OpenID (唯一)    |
| difficulty_level | VARCHAR(32) | NOVICE/SKILLED/EXPERT |
| total_score      | INT         | 累计评分              |

#### 动作定义表 (`move`)
| 字段           | 类型         | 描述                           |
| :------------- | :----------- | :----------------------------- |
| id             | VARCHAR(32)  | 动作编码 (PK, 如 `m_squat`)    |
| name           | VARCHAR(64)  | 动作名称                       |
| difficulty     | VARCHAR(32)  | 适用等级                       |
| model_url      | VARCHAR(255) | ONNX 模型下载地址              |
| scoring_config | JSON         | 包含角度阈值、判定灵敏度等配置 |

#### 训练课程表 (`training_session`)
| 字段       | 类型        | 描述                |
| :--------- | :---------- | :------------------ |
| id         | BIGINT (PK) | 课程 ID (自增)      |
| name       | VARCHAR(64) | 课程标题            |
| difficulty | VARCHAR(32) | 课程整体难度        |
| duration   | INT         | 预估训练时长 (分钟) |

#### 课程-动作关系表 (`session_move_relation`)
| 字段             | 类型   | 描述                     |
| :--------------- | :----- | :----------------------- |
| session_id       | BIGINT | 对应课程 ID              |
| move_id          | BIGINT | 对应动作 ID              |
| sort_order       | INT    | 在该课中的序号           |
| duration_seconds | INT    | 该组动作的执行时长或次数 |

---

## 3. API 参考 (API Reference)

所有接口统一采用 RESTful 风格，返回格式遵循通用响应格式：
```json
{
  "success": true,
  "code": "200",
  "message": "操作成功",
  "data": { ... },
  "timestamp": 1700000000000
}
```

### 3.1 身份认证 (`fitness-user`)

#### 1. 统一登录接口
**端点:** `POST /api/auth`

**请求示例:**
```json
{
  "type": "login_phone",
  "phone": "13800138000",
  "code": "1234"
}
```
**字段说明:**
- `type`: 登录类型，可选 `login_phone` (手机号) 或 `login_wechat` (微信)。
- `phone`: 手机号，仅在 `type` 为 `login_phone` 时必填。
- `code`: 验证码或微信授权 `code`。

**响应示例:**
```json
{
  "success": true,
  "data": {
    "id": "1",
    "nickname": "GymHero",
    "phone": "138****8000",
    "avatar": "https://oss.com/avatar.jpg",
    "token": "eyJhbG..."
  }
}
```
**字段说明:**
- `id`: 用户唯一 ID。
- `nickname`: 用户昵称。
- `phone`: 脱敏后的手机号。
- `avatar`: 头像链接。
- `token`: 后续请求需携带的 JWT Token (放入 `Authorization: Bearer <token>`)。

#### 2. 用户引导/初始化设置
**端点:** `POST /api/auth/onboarding`

**请求示例:**
```json
{
  "userId": "1",
  "difficultyLevel": "expert"
}
```
**字段说明:**
- `userId`: 用户 ID。
- `difficultyLevel`: 设定的初始难度 (`novice`, `skilled`, `expert`)。

**响应示例:**
```json
{
  "success": true,
  "data": {
    "scoringTolerance": 5,
    "recommendedPlan": "plan_starter"
  }
}
```

---

### 3.2 动作与内容 (`fitness-content`)

#### 1. 获取内容库
**端点:** `GET /api/library`

**请求参数:**
- `difficultyLevel` (Optional): 难度过滤，默认为 `novice`。

**响应示例:**
```json
{
  "success": true,
  "data": {
    "moves": [
      {
        "id": "m_squat",
        "name": "深蹲",
        "modelUrl": "https://oss.com/squat.onnx",
        "scoringConfig": { "angleThreshold": 20 }
      }
    ],
    "sessions": [
      {
        "id": "s_101",
        "name": "晨间唤醒",
        "difficulty": "novice",
        "duration": 15
      }
    ]
  }
}
```
**字段说明:**
- `moves`: 动作列表。`scoringConfig` 包含算法判别所需的阈值。
- `sessions`: 训练课列表。`duration` 为预估分钟数。

---

### 3.3 AI 评分与模型 (`fitness-ai`)

#### 1. 动作实时评分
**端点:** `POST /api/ai/score`

**请求示例:**
```json
{
  "moveId": "m_squat",
  "data": {
    "keypoints": [
      { "x": 0.5, "y": 0.5, "score": 0.9 }
    ],
    "userId": "1"
  }
}
```
**字段说明:**
- `moveId`: 正在进行的动作 ID。
- `data.keypoints`: 人体 17 个关键点坐标及置信度。

**响应示例:**
```json
{
  "success": true,
  "data": {
    "success": true,
    "score": 85,
    "feedback": ["下蹲深度完美", "注意膝盖不要内扣"]
  }
}
```
**字段说明:**
- `score`: 本次动作的得分 (0-100)。
- `feedback`: 针对性的反馈文本列表。

#### 2. 模型版本检查
**端点:** `GET /api/core/models/latest`

**请求参数:**
- `platform`: `ios` 或 `android`。
- `currentVersion`: 当前设备上的模型版本。

**响应示例:**
```json
{
  "success": true,
  "data": {
    "hasUpdate": true,
    "data": {
      "version": "1.1.0",
      "downloadUrl": "https://oss.com/pose_v1.1.onnx",
      "md5": "a3f8...",
      "forceUpdate": false
    }
  }
}
```

---

### 3.4 数据采集 (`fitness-data`)

#### 1. 埋点数据批量上报
**端点:** `POST /api/data/collect`

**请求示例:**
```json
{
  "sessionId": "s_789",
  "items": [
    { "type": "score", "moveId": "m_squat", "score": 90 },
    { "type": "heart_rate", "value": 120 }
  ]
}
```
**字段说明:**
- `sessionId`: 当前训练会话 ID。
- `items`: 混合数据项列表，将直接被打入 Kafka。

---

## 4. 数据流转路径 (Data Flow)

### 4.1 核心评分流
1. **客户端**: 运行 Pose Estimation 获取人体关键点。
2. **边缘计算 (可选)**: 客户端运行 ONNX 执行初步判定。
3. **上报**: 调用 `/api/ai/score` 进行云端复核。
4. **持久化**: 结果通过 Kafka 发送，`fitness-data` 消费者将其存入 MySQL (积分更新) 和 Doris (深度分析)。

### 4.2 模型交付与分发
1. **供应商/模型组**: 上传训练好的 `.onnx` 文件至 OSS。
2. **后端管理**: 更新 `move` 表中的 `model_url`。

---

## 5. 模型行为假设与规范 (Model Assumptions & Specifications)

由于 AI 模型目前处于研发/交付阶段，后端逻辑基于以下量化假设构建。请模型组/供应商在交付时以此作为验收基准。

### 5.1 输入数据规范 (Input Expectations)
*   **关键点格式**: 采用 COCO 17 点标准。后端假设客户端上传的 `keypoints` 包含 `(x, y)` 归一化坐标（范围 `0.0` - `1.0`）及置信度分数 `score`。
*   **采样频率**: 后端评分接口预期客户端的采样间隔不低于 **10fps**，以确保相似度计算的连续性。

### 5.2 评分算力表现 (Performance Benchmarks)
*   **单机并发性能**: 
    - 接口目标响应时间（P99）应控制在 **200ms** 以内。
    - 在 Java 21 虚拟线程支持下，单个通用型实例（4C8G）应能支撑 **500 QPS** 的评分请求。
*   **推理引擎兼容性**: 后端模型版本接口通过 MD5 和版本号语义进行分发，假设交付物为 **ONNX OPSet 13+** 兼容格式。

### 5.3 核心算法假设 (Scoring Logic Assumptions)
| 维度             | 假设说明                                                                                              | 量化指标参考                                   |
| :--------------- | :---------------------------------------------------------------------------------------------------- | :--------------------------------------------- |
| **判定准确度**   | 以标准库中的“标准向量”作为 100 分基准，计算用户实时向量的余弦相似度。                                 | $\text{similarity} \ge 0.95$ 判定为“完美”      |
| **容差调节**     | 通过 `scoring_config` 中的 `angleThreshold` 进行动态缩放。                                            | 每增加 1 度误差，评分加权扣除约 2-3 分         |
| **用户等级补偿** | 初学者 (`novice`) 在判定时，相似度结果将获得固定 **+10%** 的偏置补偿，以提供更好的反馈激励。          | $\text{final\_score} = \text{base} \times 1.1$ |
| **反馈时效性**   | 反馈建议列表 (`feedback`) 必须在得分结果生成出的同时，根据偏差最大的关键点索引（Index）自动匹配文本。 | 索引映射响应延迟 $< 50ms$                      |

### 5.4 交付物清单 (Deliverables)
1.  **ONNX 权重文件**: `.onnx`。
2.  **动作标准模板**: 一个包含 17 个关键点的 JSON 向量，用于后端进行余弦相似度比对。
3.  **反馈规则定义**: 一个逻辑映射表，例如：`"left_knee_angle > 120" -> "下蹲不够深"`。

---

## 6. 技术演进与性能优化策略 (Technical Strategy)

针对 1000+ 并发用户及未来扩展需求，制定以下架构演进与优化策略。

### 6.1 微服务拆分策略 (Microservice Decomposition)
为应对 AI 评分模块 (`fitness-ai`) 日益增长的计算密集型负载，计划将其从单体中剥离。

*   **服务边界定义**:
    *   **Scoring Service (AI 核心)**: 独立部署，负责无状态的 `OpenCV/ONNX` 推理与相似度计算。独占 GPU 或高频 CPU 资源。
    *   **Data Service (数据湖)**: 负责海量埋点数据的清洗与落库。
*   **Shared Kernel (共享内核)**:
    *   仅共享 DTO (`ScoringRequest`, `MoveDTO`) 与基础工具类 (`MathUtil`, `JwtUtil`)。
    *   通过 Maven BOM 管理版本，避免依赖地狱。
*   **通信协议**:
    *   **同步 (实时评分)**: 采用 **gRPC (Protobuf)** 替代 HTTP REST，以在服务间获得更低延迟（< 5ms）和更小的传输体积。
    *   **异步 (数据采集)**: 维持 Kafka 管道，解耦评分产生与数据分析。

### 6.2 高并发性能调优 (Performance Tuning)

为确保在 Java 21 虚拟线程环境下实现 P99 < 200ms：

#### 数据库连接池 (HikariCP)
*   **问题**: Virtual Threads 数目庞大，但数据库连接是稀缺物理资源。
*   **配置优化**:
    *   `maximum-pool-size`: 设置为 **50-60**。过大不仅无益，反而导致上下文切换开销。
    *   `connection-timeout`: 缩短至 **3000ms**，快速失败，避免线程长时间阻塞。
    *   **防钉死 (Pinning)**: 确保 MySQL 驱动更新至 **8.3+**，避免在 `synchronized` 块中进行 I/O 操作导致虚拟线程无法卸载 (Unmount)。

#### Kafka 消费者优化
*   **ACK 模式**: 调整为 `Batch` 模式。
    *   `spring.kafka.listener.ack-mode: batch`
    *   `spring.kafka.listener.type: batch`
*   **吞吐量**:
    *   增加 `concurrency` 至 **3-5** (根据分区数)。
    *   Database Batch Insert: 消费者端积累 100~500 条记录后，使用 MyBatis-Plus 的 `saveBatch` 一次性写入，避免单条 Insert 造成的 RTT 浪费。

### 6.3 移动端协同与数据同步 (Mobile-to-Cloud Synergy)

针对 React Native 前端与 Spring Boot 后端的交互，最大限度降低移动端功耗与延迟。

1.  **端侧推理优先 (Edge AI)**:
    *   利用 `ONNX Runtime Web/Mobile` 在手机本地进行 `Keypoints` 提取与初步评分 (Level 1)。
    *   **优势**: 零网络延迟，保护隐私，极大节省带宽。
    *   **云端复核**: 仅在“结算”或“挑战模式”下，抽样上传关键点至云端进行防作弊校验。

2.  **数据同步协议 (Data Sync)**:
    *   **Protocol Buffers**: 替代 JSON。关键点数组 (`float[]`) 使用 Protobuf 序列化可减少 **60%** 数据体积。
    *   **WebSocket / QUIC**: 建立长连接通道。
        *   避免 HTTP 1.1 的频繁握手开销。
        *   支持双向实时反馈（如教练端实时纠正）。

3.  **电量优化**:
    *   **批量上报**: 非实时类数据（如心率历史）缓存于本地 SQLite/Realm，仅在 WiFi 环境或每隔 60秒批量压缩上传。
    *   **差异化更新**: 仅同步模型参数的变更部分 (`Diff`), 而非全量下载新模型。
