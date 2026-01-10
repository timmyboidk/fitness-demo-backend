# Build Stage
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Run Stage
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/fitness-user/target/*.jar app.jar
ENV TZ=Asia/Shanghai
CMD ["java", "--add-opens", "java.base/java.lang=ALL-UNNAMED", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
