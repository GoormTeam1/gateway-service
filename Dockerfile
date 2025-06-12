# =======================================================
# 1. 빌드 스테이지 (Builder Stage)
# Java 17과 Gradle 8.8을 사용하여 애플리케이션을 빌드하는 환경입니다.
# CI에서 사용한 것과 동일한 이미지를 사용하여 일관성을 유지합니다.
# =======================================================
FROM gradle:8.8-jdk17 AS builder

# 작업 디렉터리 설정
WORKDIR /build

# Docker 레이어 캐싱 최적화:
# 소스코드 전체를 복사하기 전에, 변경 빈도가 낮은 의존성 관련 파일들을 먼저 복사합니다.
# 이렇게 하면, 소스코드만 변경되었을 때 Gradle이 의존성을 새로 다운로드하지 않아 빌드 속도가 빨라집니다.
COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle

# 의존성을 미리 다운로드합니다.
RUN ./gradlew dependencies --no-daemon

# 소스코드 전체를 컨테이너 안으로 복사
COPY src ./src

# Gradle 빌드를 실행하여 실행 가능한 Jar 파일을 생성합니다.
# CI의 test 스테이지에서 테스트를 수행하므로, 여기서는 스킵하여 빌드 속도를 높입니다.
RUN ./gradlew build -x test --no-daemon


# =======================================================
# 2. 최종 실행 스테이지 (Final Stage)
# 실제 애플리케이션을 실행할 최소한의 환경만 포함합니다.
# =======================================================
# 실행에 필요한 JRE(Java Runtime Environment)만 포함된 가벼운 Temurin 이미지를 사용합니다.
FROM eclipse-temurin:17-jre-jammy

# 작업 디렉터리 설정
WORKDIR /app

# 보안 강화를 위해 non-root 사용자 생성 및 사용
# root 권한으로 컨테이너를 실행하는 것은 보안상 위험할 수 있습니다.
RUN groupadd -r appuser && useradd --no-log-init -r -g appuser appuser
USER appuser

# 빌드 스테이지(builder)에서 생성된 Jar 파일만 복사해옵니다.
# 멀티스테이지 빌드의 핵심으로, JDK나 Gradle 같은 무거운 빌드 도구들은 이 이미지에 포함되지 않습니다.
COPY --from=builder /build/build/libs/*.jar app.jar

# 애플리케이션이 사용할 포트를 명시적으로 외부에 알립니다.
EXPOSE 8080

# 컨테이너가 시작될 때 이 명령어가 실행됩니다.
ENTRYPOINT ["java", "-jar", "app.jar"]