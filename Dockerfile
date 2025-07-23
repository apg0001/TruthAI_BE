# 1단계: 빌드 스테이지
FROM gradle:8.4-jdk17 AS builder
WORKDIR /app
COPY . .
RUN gradle :truthAI-server:build --no-daemon

# 2단계: 실행 스테이지
FROM openjdk:17-jdk
COPY --from=builder /app/truthAI-server/build/libs/truthAI-server-0.0.1-SNAPSHOT.jar /app/app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

