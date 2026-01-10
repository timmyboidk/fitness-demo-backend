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
