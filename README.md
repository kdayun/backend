# Wonway Backend

사용자의 자연어 상담 요청을 검증된 은행 정책과 대조하고, 처리 가능한 상담 채널을 안내하는 Spring AI 기반 MVP입니다.

## 기술 스택

- Java 21
- Spring Boot 4.1.2
- Spring AI 2.0.1
- Spring Data JPA
- MySQL
- Flyway

## 담당 영역

| 담당 | 패키지 | 역할 |
|---|---|---|
| A | `agent/**` | Spring AI, ChatMemory, 도구 호출, 최종 답변 생성 |
| B | `policy/**` | 정책 조회·검증, 채널 처리 가능 여부와 순위 계산 |
| C | `api/**`, `session/**` | REST API, 입력 검증, 상담 세션 생성·종료 |
| 공용 | `common/**` | 담당 영역 사이에서 사용하는 계약 DTO |

## 로컬 실행 준비

MySQL에 `wonway` 데이터베이스를 생성합니다.

```sql
CREATE DATABASE wonway
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
```

저장소 루트의 `.env`에 아래 환경변수를 직접 설정합니다. `.env`는 Git에 포함되지 않습니다.

```properties
OPENAI_API_KEY=
DB_USERNAME=
DB_PASSWORD=
```

실행:

```bash
./gradlew bootRun
```

테스트:

```bash
./gradlew test
```

## API

### 상태 확인

```http
GET /api/v1/health
```

### 상담 시작

```http
POST /api/v1/consultations
```

### 메시지 전송

```http
POST /api/v1/consultations/{sessionId}/messages
Content-Type: application/json

{
  "message": "전세대출 상담 채널을 추천해 주세요."
}
```

### 상담 종료

```http
DELETE /api/v1/consultations/{sessionId}
```

상담 종료 시 해당 세션의 ChatMemory가 삭제됩니다. 상담 대화는 데이터베이스에 영구 저장하지 않습니다.

## 정책 데이터 주의사항

Flyway는 정책 테이블 구조만 생성하며 실제 은행 정책은 자동으로 넣지 않습니다. 정책 데이터는 우리은행 공식 자료와 현업 검증을 거친 후 `VERIFIED` 상태로 등록해야 합니다. `SAMPLE` 데이터는 실제 추천 결과에 사용되지 않습니다.
