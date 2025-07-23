# 빌드만 수행하는 컨테이너
FROM gradle:8.13-jdk17 AS builder
#컨테이너 내 코드 경로 설정
WORKDIR /app
#project 코드를 /app 으로 복사
COPY . .
RUN ./gradlew clean build -x test

# 실행용 컨테이너
FROM openjdk:17-jdk
#빌드 컨테이너 안에서 만들어진 JAR 파일 꺼냄 -> 최종 실행 컨테이너 안에 복사
COPY --from=builder /app/truthAI-server/build/libs/truthAI-server-0.0.1-SNAPSHOT.jar /app/app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
