#!/bin/bash

# ✅ 설정값
APP_NAME="gateway-service"
JAR_NAME="gateway.jar"
APP_DIR="/home/ubuntu/backend/$APP_NAME"
LOG_FILE="$APP_DIR/$APP_NAME.log"
LOG_DIR="/home/ubuntu/backend/logs/service/$APP_NAME"
ENV_FILE="$APP_DIR/../.env"

echo "▶ [$APP_NAME] 배포 시작"

# ✅ 환경 변수 로드
if [ -f "$ENV_FILE" ]; then
  echo "▶ .env 로드 중..."
  export $(grep -v '^#' "$ENV_FILE" | xargs)
else
  echo "⚠️ .env 파일이 존재하지 않습니다: $ENV_FILE"
fi

# ✅ 로그 디렉토리 생성
echo "▶ 로그 디렉토리 생성 중..."
mkdir -p "$LOG_DIR"

# ✅ 기존 프로세스 종료
echo "▶ 기존 프로세스 종료 중..."
PID=$(pgrep -f "$JAR_NAME")
if [ -n "$PID" ]; then
  kill -9 $PID
  echo "✅ PID $PID 종료 완료"
else
  echo "ℹ️ 기존 프로세스 없음"
fi

# ✅ 새 jar 실행
echo "▶ 새 앱 실행 중..."
nohup java -jar "$LOG_DIR" > "$LOG_FILE" 2>&1 &

echo "✅ [$APP_NAME] 배포 완료. 로그: $LOG_FILE"
