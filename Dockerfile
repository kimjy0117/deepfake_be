# 1. 빌드 단계 (Gradle wrapper 사용)
FROM gradle:8.5-jdk17 AS builder
WORKDIR /app

# 캐시 최적화를 위해 먼저 의존성만 복사
COPY build.gradle settings.gradle ./
COPY gradle gradle
RUN gradle dependencies --no-daemon || true

# 나머지 소스 복사 후 빌드
COPY . .
RUN gradle bootJar --no-daemon

# 2. 실행 단계
FROM openjdk:17-jdk-slim
WORKDIR /app

# 빌드 결과물 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 환경변수 (Render의 환경변수로 오버라이드 가능)
ENV SPRING_PROFILES_ACTIVE=prod

# 8080 포트 오픈 (Spring Boot 기본)
EXPOSE 8080

# 실행 명령어
ENTRYPOINT ["java", "-jar", "app.jar"]
