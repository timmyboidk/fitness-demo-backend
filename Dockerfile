FROM eclipse-temurin:17-jre
WORKDIR /app
COPY fitness-user/target/*.jar app.jar
CMD ["java", "--add-opens", "java.base/java.lang=ALL-UNNAMED", "--add-opens", "java.base/java.io=ALL-UNNAMED", "--add-opens", "java.base/java.util=ALL-UNNAMED", "--add-opens", "java.base/java.util.concurrent=ALL-UNNAMED", "--add-opens", "java.base/java.util.concurrent.locks=ALL-UNNAMED", "-jar", "app.jar"]
