# Fitness Backend Documentation

## 1. Overview
This is a **Modular Monolith** Spring Boot application designed for high-concurrency fitness scoring and data collection. 

**Modules:**
*   `fitness-common`: Core utilities.
*   `fitness-user`: Authentication & Onboarding.
*   `fitness-content`: Movement CMS.
*   `fitness-ai`: AI Scoring & Event Streaming.
*   `fitness-data`: Analytics & Data Warehouse Ingestion.

## 2. Deployment Guide

### Prerequisites
*   JDK 21+
*   Maven 3.8+
*   Docker & Kubernetes (Optional)

### Local Development
```bash
# Build
./mvnw clean install

# Run (Main entry point is in fitness-user logic usually, or a dedicated runner)
# Ideally run the jar produced
java -jar fitness-user/target/fitness-user-0.0.1-SNAPSHOT.jar
```

### Docker Build
Create a `Dockerfile` in root:
```dockerfile
FROM eclipse-temurin:21-jre-alpine
COPY fitness-user/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### Kubernetes (K8s) Deployment
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

## 3. Maintenance

### Logging
*   Logs are output to STDOUT by default (JSON format recommended for Prod).
*   Integration with **Aliyun SLS** or **ELK Stack** is recommended.

### Monitoring
*   Expose Actuator endpoints (`/actuator/prometheus`) for Prometheus scraping.
*   Key metrics: `http_server_requests_seconds`, `jvm_memory_used_bytes`.

### Troubleshooting
*   **Slow AI Scoring:** Check Kafka lag on topic `frontend_event_stream`.
*   **OOM:** Increase Heap size or check implementation of `DataCollectionConsumer` for memory leaks.

## 4. API Reference

### Auth
*   `POST /api/auth`: Login (Phone/WeChat).
    *   Payload: `{ "type": "login_phone", "payload": { "phone": "..." } }`
*   `POST /api/auth/onboarding`: Set difficulty.

### Library
*   `GET /api/library?difficulty=novice`: List moves.

### AI & Data
*   `POST /api/ai/score`: Sync scoring.
*   `POST /api/data/collect`: Async batch data collection.
    *   Payload: `{ "sessionId": "...", "items": [...] }`
