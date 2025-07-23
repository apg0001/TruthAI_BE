# 1단계: 빌드 스테이지
FROM gradle:8.4-jdk17 AS builder
COPY . /app
WORKDIR /app
RUN gradle wrapper --gradle-version 8.13
RUN ./gradlew clean build -x test

# 2단계: 실행 스테이지
FROM openjdk:17-jdk
COPY --from=builder /app/build/libs/truthAI-server-0.0.1-SNAPSHOT.jar /app/app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

