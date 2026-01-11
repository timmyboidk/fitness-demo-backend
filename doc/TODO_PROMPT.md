
## 项目背景
该系统是一个模块化单体应用，包含用户、健身内容库、AI 评分及大数据收集模块。使用了 MySQL, Redis, Kafka 等中间件。

## 任务清单

### 1. AI 评分算法集成 (`fitness-ai`)
- **目标**: 替换 `UserScoringServiceImpl` 中的随机评分逻辑。
- **要求**: 
    - 实现一套基于关键点（Keypoints）距离比对的评分算法。
    - 对比客户端上传的关键点数据与标准动作模板数据（需设计模板存储结构）。
    - 考虑不同难度等级（novice, skilled, expert）下的容差范围。

### 2. 真实认证与鉴权 (`fitness-user`)
- **目标**: 移除 Mock Token，实现基于 JWT 的安全认证。
- **要求**:
    - 集成 `jjwt` 或 `java-jwt` 库。
    - 实现 `JwtUtil` 用于生成和解析 Token。
    - 编写 `LoginInterceptor` 或集成 Spring Security 对 `/api/**`（除登录外）进行拦截验证。
    - 实现真实的微信小程序登录流程（调用 `auth.code2Session` 接口）。

### 3. 内容库增强 (`fitness-content`)
- **目标**: 完善健身课程（Sessions）逻辑。
- **要求**:
    - 创建 `Session` 和 `SessionMove` 实体及对应的数据库表。
    - 实现获取训练课程列表及课程详情的接口。
    - 课程详情应包含一组有序的运动动作。

### 4. 数据采集持久化 (`fitness-data`)
- **目标**: 实现真正的多级存储。
- **要求**:
    - 在 `DataCollectionConsumer` 中，将 Kafka 消费到的事件持久化。
    - 核心指标（如用户得分）写入 MySQL。
    - 埋点类原始数据（如原始坐标映射）异步写入大数据存储组件（模拟或实现 JDBC 写入 Doris）。

### 5. 安全与运维优化 (`fitness-common` & 配置)
- **目标**: 确保系统具备公网部署的安全性与可观测性。
- **要求**:
    - **加解密强化**: 将 `EncryptTypeHandler` 中的 AES 密钥改为从 `application-{env}.yml` 配置读取，并支持环境变量覆盖。
    - **CORS 配置**: 实现全局跨域配置，仅允许特定的前端域名访问。
    - **Swagger 增强**: 为所有接口添加 Swagger/OpenAPI 注解，并配置生产环境关闭文档入口。
    - **健康检查**: 集成 Spring Boot Actuator，并配置 `/actuator/health` 的安全访问。

### 6. 工程化与部署准备
- **目标**: 一键部署并支持环境隔离。
- **要求**:
    - **环境隔离**: 建立 `application-dev.yml`, `application-prod.yml`，区分数据库、Redis 和 Kafka 的连接信息。
    - **数据库迁移**: 集成 Flyway 或 Liquibase，确保数据库表结构的版本受控。
    - **Docker 优化**: 编写高效的 `Dockerfile`（多阶段构建），并提供 `docker-compose.prod.yml` 用于一键启动生产环境中间件。
    - **日志审计**: 配置 Logback 分文件存储日志（info, error 隔离），并设置滚动策略防止磁盘撑爆。

## 交付要求
1. **代码质量**: 严格遵循阿里巴巴 Java 开发规范。
2. **安全性**: 必须防止 SQL 注入、越权访问。JWT 必须设置合理的过期时间及 Refresh Token 机制。
3. **注释**: 所有新代码必须包含规范的简体中文 Javadoc。
4. **健壮性**: 必须处理异常边界，确保不因单点失败（如 Kafka 慢）阻塞主业务。
5. **性能**: 重点关注数据库查询优化（加索引）及 Redis 缓存一致性。

