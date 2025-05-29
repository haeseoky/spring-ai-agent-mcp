# Spring AI Agent with MCP

Spring Boot 기반의 AI 에이전트 애플리케이션으로, OpenAI와 MCP(Model Context Protocol)를 통합하여 다양한 도구를 활용할 수 있습니다.

## 주요 기능

- 🤖 **AI 채팅**: Spring AI를 통한 OpenAI 통합
- 🔧 **MCP 도구 통합**: 파일 시스템, 웹 검색 등 다양한 도구 지원
- 🔄 **스트리밍 응답**: Server-Sent Events를 통한 실시간 스트리밍
- 💾 **대화 기록 관리**: 대화 내용 저장 및 관리
- 🌐 **RESTful API**: 웹 기반 인터페이스를 위한 API 제공

## 기술 스택

- **Backend**: Spring Boot 3.5.0, Kotlin 1.9.25
- **AI Integration**: Spring AI, OpenAI API
- **Build Tool**: Gradle 8.14
- **Java Version**: 21

## 프로젝트 구조

```
src/
├── main/
│   ├── kotlin/
│   │   └── com/ocean/agent/
│   │       ├── config/          # 설정 클래스
│   │       ├── controller/      # REST 컨트롤러
│   │       ├── domain/          # 도메인 모델
│   │       └── service/         # 비즈니스 로직
│   └── resources/
│       └── application.properties
└── test/
    └── kotlin/                  # 테스트 코드
```

## 설치 및 실행

### 사전 요구사항

- JDK 21 이상
- OpenAI API 키

### 설정

1. `application.properties` 파일에 OpenAI API 키 설정:
```properties
spring.ai.openai.api-key=${OPENAI_API_KEY:your-api-key-here}
```

2. 환경변수로 API 키 설정:
```bash
export OPENAI_API_KEY=your-actual-api-key
```

### 빌드 및 실행

```bash
# 빌드
./gradlew clean build

# 실행
./gradlew bootRun

# 또는 JAR 파일로 실행
java -jar build/libs/agent-0.0.1-SNAPSHOT.jar
```

기본 포트는 8081입니다. 다른 포트로 실행하려면:
```bash
java -jar build/libs/agent-0.0.1-SNAPSHOT.jar --server.port=9090
```

## API 엔드포인트

### 채팅 API

- **POST** `/api/chat` - 일반 채팅 요청
- **POST** `/api/chat/stream` - 스트리밍 채팅 요청
- **GET** `/api/chat/conversations/{conversationId}` - 대화 기록 조회
- **DELETE** `/api/chat/conversations/{conversationId}` - 대화 기록 삭제
- **GET** `/api/chat/health` - 헬스 체크

### MCP API

- **GET** `/api/mcp/servers` - 모든 MCP 서버 목록 조회
- **GET** `/api/mcp/servers/enabled` - 활성화된 MCP 서버 목록 조회
- **GET** `/api/mcp/tools` - 사용 가능한 도구 목록 조회
- **POST** `/api/mcp/tools/execute` - 도구 실행
- **GET** `/api/mcp/health` - 헬스 체크

## 사용 예시

### 채팅 요청

```bash
curl -X POST http://localhost:8081/api/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "안녕하세요! 오늘 날씨가 어떤가요?",
    "conversationId": "test-conversation"
  }'
```

### 스트리밍 채팅

```bash
curl -X POST http://localhost:8081/api/chat/stream \
  -H "Content-Type: application/json" \
  -H "Accept: text/event-stream" \
  -d '{
    "message": "스트리밍 테스트입니다."
  }'
```

### MCP 도구 실행

```bash
curl -X POST http://localhost:8081/api/mcp/tools/execute \
  -H "Content-Type: application/json" \
  -d '{
    "toolName": "read_file",
    "arguments": {
      "path": "/path/to/file.txt"
    }
  }'
```

## 개발

### 테스트 실행

```bash
./gradlew test
```

### 코드 스타일

Kotlin 공식 코딩 컨벤션을 따릅니다.

## 라이선스

이 프로젝트는 MIT 라이선스를 따릅니다.

## 문의

질문이나 제안사항이 있으시면 이슈를 생성해주세요.
