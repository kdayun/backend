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

## 테스트 시나리오: 자동이체 날짜 변경

예시 요청:

```text
월급날이 바뀌어서 엄마 생활비 보내는 날을 20일에서 26일로 바꾸고 싶어요.
```

에이전트는 먼저 매달 자동으로 보내는 자동이체인지 직접 송금하는지 확인합니다. 자동이체라고 확인된 경우에만 다음 표준 조건으로 정책을 조회합니다.

```text
intent: AUTO_TRANSFER_DATE_CHANGE
customerType: INDIVIDUAL
productType: ACCOUNT_AUTO_TRANSFER
transferMethod: AUTOMATIC
currentTransferDay: 20
requestedTransferDay: 26
```

추천 순위는 서버 코드에서 결정하며 모델이 임의로 바꿀 수 없습니다.

| 순위 | 채널 | 점수 | 기준 |
|---|---|---:|---|
| 1 | 우리WON뱅킹 | 100 | 스마트뱅킹에서 본인확인 후 직접 변경 가능, 이동 단계 최소 |
| 2 | 개인 인터넷뱅킹 | 85 | `자동이체조회/등록/변경` 메뉴 제공, 변경 이용시간 06:00~23:59 |
| 3 | 텔레뱅킹·고객센터 | 65 | 텔레뱅킹 신청 가능, 전화상담은 평일 09:00~18:00 |

본인확인이 불가능하면 모바일·인터넷 채널을 제외하고 영업점을 안전한 대안으로 안내합니다. 변경 신청은 기존 이체지정일 전 영업일까지 해야 합니다.

현재 MVP 시나리오는 로그인한 사용자를 전제로 합니다. 운영 단계에서는 대화 내용이 아니라 프론트의 실제 인증 상태를 서버가 전달하도록 계약을 확장해야 합니다.

정책 근거:

- [우리은행 개인 인터넷뱅킹 이용시간](https://spot.wooribank.com/pot/Dream?withyou=CQIBG0024)
- [우리은행 자동이체조회/등록/변경](https://spib.wooribank.com/pib/Dream?basePage=PSTRS0018&nextPage=ps&withyou=CMCOM0001)
- [우리은행 계좌간 자동이체약관](https://spot.wooribank.com/pot/bbs?cmd=download&BOARD_ID=B00190&ARTICLE_ID=17102&ATTACH_ID=26783)

## 정책 데이터 주의사항

Flyway는 정책 테이블 구조만 생성하며 실제 은행 정책은 자동으로 넣지 않습니다. 정책 데이터는 우리은행 공식 자료와 현업 검증을 거친 후 `VERIFIED` 상태로 등록해야 합니다. `SAMPLE` 데이터는 실제 추천 결과에 사용되지 않습니다.
