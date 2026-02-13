# 数据定义文档 (Data Definitions)

本文档定义了 Fitness 后端项目中所有请求 (Request)、响应 (Response)、内部传输对象 (Event) 及数据库实体 (Entity) 的完整字段说明。  
适用于后端开发、前端联调、测试编写及新成员入职参考。

---

## 1. 通用响应包装 (`Result<T>`)

所有 API 接口统一使用 `Result<T>` 作为返回值外壳。

**类路径**：`com.example.fitness.common.result.Result`

| 字段      | 类型      | 说明                                       |
| :-------- | :-------- | :----------------------------------------- |
| `success` | `boolean` | 请求是否成功，`true` 表示业务处理正常       |
| `code`    | `Integer` | 状态码，成功为 `200`，错误参见错误码表       |
| `message` | `String`  | 响应消息，成功时为 `"操作成功"`              |
| `data`    | `T`       | 业务数据载荷，具体类型由各接口定义           |

**示例**：
```json
{
  "success": true,
  "code": 200,
  "message": "操作成功",
  "data": { ... }
}
```

---

## 2. 错误码枚举 (`ErrorCode`)

**类路径**：`com.example.fitness.common.result.ErrorCode`

| 枚举常量                       | 编号   | 消息               | 使用场景                             |
| :----------------------------- | :----- | :----------------- | :----------------------------------- |
| `SUCCESS`                      | `200`  | 操作成功           | 所有成功响应                         |
| `PARAM_ERROR`                  | `400`  | 参数错误           | 请求参数校验失败                     |
| `UNAUTHORIZED`                 | `401`  | 未授权             | JWT 缺失或无效                       |
| `FORBIDDEN`                    | `403`  | 禁止访问           | 非 VIP 用户访问 PRO 内容             |
| `NOT_FOUND`                    | `404`  | 资源不存在         | 通用资源未找到                       |
| `INTERNAL_SERVER_ERROR`        | `500`  | 系统内部错误       | 服务端未知异常                       |
| `KAFKA_SEND_ERROR`             | `501`  | 消息队列发送失败   | Kafka 消息发送失败                   |
| `USER_NOT_FOUND`               | `1001` | 用户不存在         | 按 ID 查询用户未命中                 |
| `USER_ID_REQUIRED`             | `1002` | 用户 ID 必填       | 引导设置时未传 userId                |
| `INVALID_LOGIN_TYPE`           | `1003` | 不支持的登录类型   | 旧版统一登录接口 type 字段非法       |
| `LOGIN_FAILED`                 | `1004` | 登录失败           | 微信授权 code 无效                   |
| `MOVE_NOT_FOUND`               | `2001` | 健身动作不存在     | 查询动作 ID 未命中                   |
| `LIBRARY_ITEM_ALREADY_EXISTS`  | `2002` | 该项目已在库中     | 用户重复收藏同一动作                 |
| `SCORING_FAILED`               | `3001` | AI 评分计算失败    | 评分算法异常                         |
| `MODEL_NOT_FOUND`              | `3002` | 模型版本不存在     | 模型版本检查未命中                   |

---

## 3. 请求 DTO (Request Data Transfer Objects)

### 3.1 `RequestOtpRequest` — 请求验证码

**类路径**：`com.example.fitness.api.dto.RequestOtpRequest`  
**使用接口**：`POST /api/auth/request-otp`

| 字段    | 类型     | 约束                          | 说明                     |
| :------ | :------- | :---------------------------- | :----------------------- |
| `phone` | `String` | `@NotBlank`，不能为空          | 接收验证码的手机号       |

### 3.2 `VerifyOtpRequest` — 手机号验证码登录

**类路径**：`com.example.fitness.api.dto.VerifyOtpRequest`  
**使用接口**：`POST /api/auth/verify-otp`

| 字段    | 类型     | 约束                          | 说明                         |
| :------ | :------- | :---------------------------- | :--------------------------- |
| `phone` | `String` | `@NotBlank`，不能为空          | 用户手机号                   |
| `code`  | `String` | `@NotBlank`，不能为空          | 短信验证码（4-6 位数字）     |

### 3.3 `WechatLoginRequest` — 微信授权登录

**类路径**：`com.example.fitness.api.dto.WechatLoginRequest`  
**使用接口**：`POST /api/auth/wechat`

| 字段   | 类型     | 约束                          | 说明                               |
| :----- | :------- | :---------------------------- | :--------------------------------- |
| `code` | `String` | `@NotBlank`，不能为空          | 微信小程序 `wx.login()` 返回的 code |

### 3.4 `LoginRequest` — 统一登录请求（已废弃）

**类路径**：`com.example.fitness.api.dto.LoginRequest`  
**使用接口**：`POST /api/auth`（已废弃，建议使用 3.2 或 3.3）

| 字段              | 类型     | 约束                          | 说明                                                |
| :---------------- | :------- | :---------------------------- | :-------------------------------------------------- |
| `type`            | `String` | `@NotBlank`，不能为空          | 登录类型：`phone` (手机号) 或 `wechat` (微信)        |
| `phone`           | `String` | 仅 `type=phone` 时必填         | 用户手机号                                          |
| `code`            | `String` | 必填                          | 验证码（手机号登录）或微信授权 code（微信登录）      |
| `difficultyLevel` | `String` | 可选                          | 引导流程使用的难度等级                              |
| `userId`          | `String` | 可选                          | 引导流程使用的用户 ID                               |

### 3.5 `OnboardingRequest` — 首次使用引导设置

**类路径**：`com.example.fitness.api.dto.OnboardingRequest`  
**使用接口**：`POST /api/auth/onboarding`

| 字段              | 类型     | 约束                          | 说明                                          |
| :---------------- | :------- | :---------------------------- | :-------------------------------------------- |
| `userId`          | `String` | `@NotBlank`，不能为空          | 目标用户 ID                                   |
| `difficultyLevel` | `String` | 可选，默认 `novice`            | 难度等级：`novice`（初学者）/ `skilled`（熟练）/ `expert`（专家） |

### 3.6 `AuthRequest` — 通用认证请求

**类路径**：`com.example.fitness.api.dto.AuthRequest`  
**说明**：底层抽象的通用认证请求，通过 `type` 和 `payload` 动态路由。

| 字段      | 类型                  | 约束 | 说明                                                 |
| :-------- | :-------------------- | :--- | :--------------------------------------------------- |
| `type`    | `String`              | —    | 认证类型标识：`login_phone` / `login_wechat`          |
| `payload` | `Map<String, Object>` | —    | 依认证类型不同，携带 `phone`/`code` 等动态字段        |

### 3.7 `ScoringRequest` — AI 动作评分请求

**类路径**：`com.example.fitness.api.dto.ScoringRequest`  
**使用接口**：`POST /api/ai/score`

| 字段     | 类型                  | 约束   | 说明                                                                   |
| :------- | :-------------------- | :----- | :--------------------------------------------------------------------- |
| `moveId` | `String`              | 必填   | 正在执行的动作 ID（如 `m_squat`）                                       |
| `data`   | `Map<String, Object>` | 必填   | 关键点数据负载，需包含 `keypoints` 数组及 `userId` 字段                  |

**`data.keypoints` 元素格式**：

| 字段    | 类型     | 说明                                    |
| :------ | :------- | :-------------------------------------- |
| `x`     | `double` | 关键点 X 坐标（归一化 0.0 ~ 1.0）       |
| `y`     | `double` | 关键点 Y 坐标（归一化 0.0 ~ 1.0）       |
| `score` | `double` | 关键点检测置信度（0.0 ~ 1.0）            |

---

## 4. 响应 DTO (Response Data Transfer Objects)

### 4.1 `UserDTO` — 用户信息

**类路径**：`com.example.fitness.api.dto.UserDTO`  
**使用场景**：登录成功响应、个人资料查询响应

| 字段       | 类型     | 说明                                         |
| :--------- | :------- | :------------------------------------------- |
| `id`       | `String` | 用户唯一标识（数据库自增 ID 转字符串）        |
| `nickname` | `String` | 用户昵称                                     |
| `phone`    | `String` | 用户手机号（脱敏：仅展示前三后四位）          |
| `avatar`   | `String` | 用户头像 URL                                 |
| `token`    | `String` | JWT 认证 Token（仅登录响应时有值，查询为 `null`）|

### 4.2 `ScoringResponse` — AI 评分响应

**类路径**：`com.example.fitness.api.dto.ScoringResponse`  
**使用场景**：`POST /api/ai/score` 响应体

| 字段       | 类型           | 说明                                       |
| :--------- | :------------- | :----------------------------------------- |
| `success`  | `boolean`      | 评分是否计算成功                           |
| `score`    | `Integer`      | 最终评分（0 ~ 100），失败时为 `0`           |
| `feedback` | `List<Object>` | 反馈建议列表（如 `["完美！保持这个节奏"]`）  |

### 4.3 `LibraryResponse` — 动作库响应

**类路径**：`com.example.fitness.api.dto.LibraryResponse`  
**使用场景**：`GET /api/library` 响应体

| 字段       | 类型              | 说明             |
| :--------- | :---------------- | :--------------- |
| `moves`    | `List<MoveDTO>`   | 健身动作列表     |
| `sessions` | `List<SessionDTO>`| 训练课程列表     |

### 4.4 `MoveDTO` — 动作信息

**类路径**：`com.example.fitness.api.dto.MoveDTO`  
**使用场景**：嵌套在 `LibraryResponse.moves` 中

| 字段            | 类型                  | 说明                                      |
| :-------------- | :-------------------- | :---------------------------------------- |
| `id`            | `String`              | 动作唯一标识（如 `m_squat`）               |
| `name`          | `String`              | 动作名称（如 `深蹲`）                      |
| `modelUrl`      | `String`              | ONNX AI 模型下载地址                       |
| `scoringConfig` | `Map<String, Object>` | 评分配置（如 `{"angleThreshold": 20}`）     |

### 4.5 `SessionDTO` — 训练课程信息

**类路径**：`com.example.fitness.api.dto.SessionDTO`  
**使用场景**：嵌套在 `LibraryResponse.sessions` 中

| 字段         | 类型            | 说明                                         |
| :----------- | :-------------- | :------------------------------------------- |
| `id`         | `String`        | 课程唯一标识                                 |
| `name`       | `String`        | 课程名称（如 `晨间唤醒`）                    |
| `difficulty` | `String`        | 课程难度等级                                 |
| `duration`   | `Integer`       | 预估训练时长（分钟）                         |
| `coverUrl`   | `String`        | 课程封面图片 URL                             |
| `moves`      | `List<MoveDTO>` | 关联的动作列表（列表接口返回空，详情接口填充）|

---

## 5. 内部传输对象 (Internal Event)

### 5.1 `ScoringResultEvent` — 评分结果 Kafka 事件

**类路径**：`com.example.fitness.api.dto.ScoringResultEvent`  
**传输方式**：通过 Kafka Topic `frontend_event_stream` 异步发送  
**生产者**：`UserScoringServiceImpl`  
**消费者**：`DataCollectionConsumer`

| 字段        | 类型                  | 说明                                           |
| :---------- | :-------------------- | :--------------------------------------------- |
| `userId`    | `String`              | 执行动作的用户 ID（可能为 `"unknown"`）          |
| `moveId`    | `String`              | 被评分的动作 ID                                |
| `score`     | `Integer`             | 评分结果（0 ~ 100）                            |
| `extraData` | `Map<String, Object>` | 附加数据（如 `{"duration": 5}`）                |
| `timestamp` | `LocalDateTime`       | 评分时间戳                                     |

---

## 6. 数据库实体 (Entity)

### 6.1 `User` — 用户实体

**类路径**：`com.example.fitness.user.model.entity.User`  
**映射表**：`user`

| 字段              | 类型            | 数据库类型    | 说明                                      |
| :---------------- | :-------------- | :------------ | :---------------------------------------- |
| `id`              | `Long`          | `BIGINT (PK)` | 用户唯一标识，自增主键                    |
| `phone`           | `String`        | `VARCHAR(32)` | 手机号（AES 加密存储，使用 `EncryptTypeHandler`）|
| `nickname`        | `String`        | `VARCHAR(64)` | 用户昵称                                  |
| `password`        | `String`        | `VARCHAR(255)`| 密码（当前未使用）                        |
| `openId`          | `String`        | `VARCHAR(64)` | 微信 OpenID（唯一索引）                   |
| `sessionKey`      | `String`        | `VARCHAR(64)` | 微信小程序会话密钥                        |
| `difficultyLevel` | `String`        | `VARCHAR(32)` | 运动难度等级（`novice`/`skilled`/`expert`）|
| `avatar`          | `String`        | `VARCHAR(255)`| 用户头像 URL                              |
| `totalScore`      | `Integer`       | `INT`         | 累计训练得分（由 Kafka 消费者异步更新）    |
| `totalDuration`   | `Integer`       | `INT`         | 累计训练时长（秒）                        |
| `createdAt`       | `LocalDateTime` | `DATETIME`    | 创建时间                                  |
| `updatedAt`       | `LocalDateTime` | `DATETIME`    | 最后更新时间                              |

### 6.2 `Move` — 健身动作实体

**类路径**：`com.example.fitness.content.model.entity.Move`  
**映射表**：`move`

| 字段               | 类型            | 数据库类型     | 说明                                       |
| :----------------- | :-------------- | :------------- | :----------------------------------------- |
| `id`               | `String`        | `VARCHAR(32) PK` | 动作唯一编码（如 `m_squat`），手动输入     |
| `name`             | `String`        | `VARCHAR(64)`  | 动作名称                                   |
| `difficulty`       | `String`        | `VARCHAR(32)`  | 适用难度等级                               |
| `modelUrl`         | `String`        | `VARCHAR(255)` | ONNX 模型下载 URL                          |
| `scoringConfigJson`| `String`        | `JSON`         | 评分配置 JSON（含角度阈值等参数）           |
| `createdAt`        | `LocalDateTime` | `DATETIME`     | 创建时间                                   |

### 6.3 `Session` — 训练课程实体

**类路径**：`com.example.fitness.content.model.entity.Session`  
**映射表**：`training_session`

| 字段         | 类型            | 数据库类型    | 说明                     |
| :----------- | :-------------- | :------------ | :----------------------- |
| `id`         | `Long`          | `BIGINT (PK)` | 自增主键                 |
| `name`       | `String`        | `VARCHAR(64)` | 课程名称                 |
| `difficulty` | `String`        | `VARCHAR(32)` | 课程整体难度             |
| `duration`   | `Integer`       | `INT`         | 预估时长（分钟）         |
| `coverUrl`   | `String`        | `VARCHAR(255)`| 封面图片 URL             |
| `createdAt`  | `LocalDateTime` | `DATETIME`    | 创建时间                 |
| `updatedAt`  | `LocalDateTime` | `DATETIME`    | 最后更新时间             |

### 6.4 `SessionMove` — 课程-动作关联实体

**类路径**：`com.example.fitness.content.model.entity.SessionMove`  
**映射表**：`session_move_relation`

| 字段              | 类型            | 数据库类型    | 说明                           |
| :---------------- | :-------------- | :------------ | :----------------------------- |
| `id`              | `Long`          | `BIGINT (PK)` | 自增主键                       |
| `sessionId`       | `Long`          | `BIGINT`      | 关联的课程 ID（外键）          |
| `moveId`          | `Long`          | `BIGINT`      | 关联的动作 ID（外键）          |
| `sortOrder`       | `Integer`       | `INT`         | 动作在课程中的排列顺序         |
| `durationSeconds` | `Integer`       | `INT`         | 该动作在此课程中的执行时长（秒）|
| `createdAt`       | `LocalDateTime` | `DATETIME`    | 创建时间                       |

### 6.5 `UserLibrary` — 用户个人内容库实体

**类路径**：`com.example.fitness.content.model.entity.UserLibrary`  
**映射表**：`user_library`

| 字段       | 类型            | 数据库类型    | 说明                                  |
| :--------- | :-------------- | :------------ | :------------------------------------ |
| `id`       | `Long`          | `BIGINT (PK)` | 自增主键                              |
| `userId`   | `Long`          | `BIGINT`      | 用户 ID（外键）                       |
| `itemId`   | `String`        | `VARCHAR(64)` | 收藏的内容 ID（动作 ID 或课程 ID）    |
| `itemType` | `String`        | `VARCHAR(32)` | 内容类型：`move`（动作）/ `session`（课程）|
| `createdAt`| `LocalDateTime` | `DATETIME`    | 收藏时间                              |

### 6.6 `UserStats` — 用户训练统计视图实体

**类路径**：`com.example.fitness.data.model.entity.UserStats`  
**映射表**：`user`（与 `User` 实体共享同一表，仅映射统计相关字段）

| 字段            | 类型            | 数据库类型    | 说明                   |
| :-------------- | :-------------- | :------------ | :--------------------- |
| `id`            | `Long`          | `BIGINT (PK)` | 用户 ID                |
| `totalScore`    | `Integer`       | `INT`         | 累计训练得分           |
| `totalDuration` | `Integer`       | `INT`         | 累计训练时长（秒）     |
| `updatedAt`     | `LocalDateTime` | `DATETIME`    | 最后更新时间           |

---

## 7. 接口与数据定义速查索引

| 接口端点                       | HTTP 方法 | 请求体类型            | 响应 `data` 类型           |
| :----------------------------- | :-------- | :-------------------- | :------------------------- |
| `/api/auth/request-otp`        | POST      | `RequestOtpRequest`   | `Map` (`expiresIn`)        |
| `/api/auth/verify-otp`         | POST      | `VerifyOtpRequest`    | `UserDTO`                  |
| `/api/auth/wechat`             | POST      | `WechatLoginRequest`  | `UserDTO`                  |
| `/api/auth`                    | POST      | `LoginRequest`        | `UserDTO` *(已废弃)*       |
| `/api/auth/onboarding`         | POST      | `OnboardingRequest`   | `Map` (`scoringTolerance`) |
| `/api/auth/user/stats`         | POST      | `Map<String, Object>` | `void`                     |
| `/api/library`                 | GET       | — (查询参数)           | `LibraryResponse`          |
| `/api/library`                 | POST      | `Map<String, Object>` | `void`                     |
| `/api/ai/score`                | POST      | `ScoringRequest`      | `ScoringResponse`          |
| `/api/core/models/latest`      | GET       | — (查询参数)           | `Map` (模型更新信息)       |
| `/api/data/collect`            | POST      | `Map<String, Object>` | `void`                     |
| `/api/user/stats`              | GET       | —                     | `Map` (统计数据)           |
| `/api/user/profile`            | GET       | —                     | `UserDTO`                  |
| `/api/social/leaderboard`      | GET       | — (查询参数)           | `List<Map>` (排行榜)       |
| `/api/social/feed`             | GET       | —                     | `List<Map>` (动态列表)     |
| `/api/pay/verify`              | POST      | `Map<String, Object>` | `Map` (VIP 状态)           |
