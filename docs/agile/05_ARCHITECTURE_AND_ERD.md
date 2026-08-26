# GearHub Campus 아키텍처와 ERD

## 1. 설계 원칙

- 그룹마다 서버를 만들지 않고 단일 배포에서 groupId로 워크스페이스를 분리한다.
- 서비스는 자신이 소유한 업무 상태와 테이블만 변경한다.
- 대여 재고는 Asset, 요청 상태는 Request, 예산 상태는 Budget이 판단한다.
- 동기 호출은 즉시 확인이 필요한 권한·재고에, Kafka는 예산 결과와 분석 이력에 사용한다.
- AI는 운영 요청 DB를 직접 해석하지 않고 대여 생명주기 이벤트를 자체 이력으로 축적한다.
- 물리 테이블 이름을 재사용하더라도 업무 용어와 경계를 문서에서 분명히 한다.

## 2. 주요 아키텍처 결정

### ADR-001: 그룹별 동적 워크스페이스

결정: 하나의 Web과 MSA를 배포하고 /groups/{groupId} 아래에서 데이터 범위를 나눈다.

이유:

- 새 그룹 생성 시 배포가 필요 없다.
- 공통 기능과 보안 패치를 한 번에 적용한다.
- 학교 공용 자산과 그룹 전용 자산을 조합할 수 있다.

대가:

- 모든 서비스가 groupId와 멤버십을 일관되게 검증해야 한다.
- 현재 구조만으로 여러 대학 간 완전한 테넌트 격리를 보장하지 않는다.

### ADR-002: 대여와 도입 요청을 하나의 Request 서비스에서 관리

결정: requestType LOAN과 PURCHASE를 같은 상태 관리 서비스가 소유한다.

이유:

- 구성원의 신청, 관리자 검토, 상태 조회 패턴을 재사용한다.
- 실습 템플릿의 Enrollment 실행 구조를 유지한다.

대가:

- 두 흐름의 허용 상태를 타입별로 엄격히 검증해야 한다.

### ADR-003: 관리자용 수요예측

결정: 개인 자산 추천 대신 그룹 × 카테고리의 다음 4주 수요를 예측한다.

이유:

- 대여자는 필요한 장비를 이미 정한 경우가 많다.
- 관리자는 이동·도입이라는 구체적 행동을 취할 수 있다.
- 자체 대여 데이터와 scikit-learn 평가로 AI의 근거를 설명할 수 있다.

## 3. 서비스 경계

| 업무 서비스 | 물리 구현 | 포트 | 소유 데이터 | 핵심 책임 |
|---|---|---:|---|---|
| Member | user-service | 8081 | users, campus_groups, group_memberships | 사용자·그룹·권한 |
| Asset | course-service | 8082 | courses | 자산·수량·입고 |
| Request | enrollment-service | 8083 | enrollments | 대여·반납·도입 상태 |
| Budget | payment-service | 8084 | payments | 학교 예산 승인·반려 |
| Demand Analytics | recommend-service | 8085 | analytics_* | 이벤트, 학습·평가·예측 |
| Web | vue-frontend | 3000 | 브라우저 상태 | 그룹 워크스페이스 |
| Gateway | 제공 이미지 | 8080 | 없음 | 인증·라우팅·사용자 헤더 |
| Auth | 제공 이미지 | 9000 | 인증 정보 | OAuth2 |
| Eureka | eureka-server | 8761 | 등록 상태 | 서비스 탐색 |

## 4. 전체 구성도

~~~mermaid
flowchart LR
    U[Browser] --> W[Vue Web :3000]
    W --> G[API Gateway :8080]
    G --> AU[Auth :9000]
    G --> M[Member :8081]
    G --> A[Asset :8082]
    G --> R[Request :8083]
    G --> B[Budget :8084]
    G --> D[Demand Analytics :8085]

    R -->|group access| M
    A -->|group access| M
    R -->|asset and stock| A
    R -->|budget request| B
    D -->|current stock| A

    B -->|payment.completed| K[Kafka :9092]
    K --> R
    R -->|rental.lifecycle| K
    K --> D

    M --> DB[(MariaDB :3379)]
    A --> DB
    R --> DB
    B --> DB
    D --> DB

    E[Eureka :8761] -. discovery .-> G
    E -. discovery .-> M
    E -. discovery .-> A
    E -. discovery .-> R
    E -. discovery .-> B
    E -. discovery .-> D
~~~

## 5. 동기 통신

| 호출 | 목적 | 실패 시 현재 동작 |
|---|---|---|
| Asset → Member | 자산 등록·조회 범위 확인 | 요청 실패 |
| Request → Member | 멤버 또는 관리자 권한 확인 | 요청 실패 |
| Request → Asset | 자산 조회, 재고 차감·복구, 입고 | 요청 실패 |
| Request → Budget | GROUP_APPROVED 뒤 예산 요청 생성 | 요청 실패 |
| Demand Analytics → Asset | 예측과 비교할 현재 재고 조회 | 예측 요약 503 |

서비스 간 참조는 다른 테이블을 직접 JOIN하지 않고 ID와 내부 API를 사용한다.

## 6. 비동기 통신

### payment.completed

발행: Budget

소비: Request

대표 payload:

~~~json
{
  "paymentId": 17,
  "userId": 2,
  "courseId": 126,
  "requestId": 409,
  "groupId": 1,
  "status": "COMPLETED"
}
~~~

- COMPLETED이면 PURCHASE 요청을 BUDGET_APPROVED로 바꾼다.
- FAILED이면 요청을 REJECTED로 바꾼다.
- requestId가 없을 때는 호환을 위해 사용자·자산의 최근 도입 요청을 찾는다.

### rental.lifecycle

발행: Request

소비: Demand Analytics

대표 payload:

~~~json
{
  "eventId": "UUID",
  "eventType": "REQUESTED",
  "occurredAt": "2026-08-27T10:30:00",
  "requestId": 225,
  "userId": 2,
  "groupId": 1,
  "assetId": 8,
  "category": "COMPUTER",
  "quantity": 1,
  "requestedFrom": "2026-08-28",
  "dueDate": "2026-09-03",
  "returnedAt": null
}
~~~

eventType은 REQUESTED, APPROVED, REJECTED, RETURN_REQUESTED, RETURNED를 사용한다. 분석 학습의 목표값은 REQUESTED 이벤트를 주 단위로 집계한 수량이다. eventId를 기본키로 upsert해 같은 이벤트의 중복 저장을 막는다.

enrollment.completed 이벤트와 대체재 조회는 제공 템플릿 계약 호환용으로 남아 있으며, 현재 관리자 AI 흐름의 입력은 rental.lifecycle이다.

## 7. 대여·반납 시퀀스

~~~mermaid
sequenceDiagram
    actor User
    participant Web
    participant Request
    participant Member
    participant Asset
    participant Kafka
    participant Analytics

    User->>Web: 기간·사유로 대여 신청
    Web->>Request: POST /api/enrollments
    Request->>Member: 그룹 멤버 확인
    Request->>Asset: 자산·가용 수량 확인
    Request->>Request: PENDING 저장
    Request->>Kafka: REQUESTED
    Kafka-->>Analytics: LIVE 이벤트 저장

    actor Manager
    Manager->>Request: 승인
    Request->>Member: 그룹 관리자 확인
    Request->>Asset: borrow 1
    Asset->>Asset: availableQuantity - 1
    Request->>Request: ACTIVE

    User->>Request: 반납 요청
    Request->>Request: RETURN_REQUESTED
    Manager->>Request: 실물 반납 확인
    Request->>Asset: return 1
    Asset->>Asset: availableQuantity + 1
    Request->>Request: RETURNED
~~~

## 8. 도입·예산·입고 시퀀스

~~~mermaid
sequenceDiagram
    actor User
    actor GroupManager
    actor SchoolAdmin
    participant Request
    participant Asset
    participant Budget
    participant Kafka

    User->>Request: 도입 요청
    Request->>Asset: PURCHASE_REQUEST 생성
    Request->>Request: PURCHASE / PENDING
    GroupManager->>Request: 그룹 승인
    Request->>Request: GROUP_APPROVED
    Request->>Budget: 예산 요청
    Budget->>Budget: PENDING
    SchoolAdmin->>Budget: 승인
    Budget->>Budget: COMPLETED
    Budget->>Kafka: payment.completed
    Kafka-->>Request: 예산 결과
    Request->>Request: BUDGET_APPROVED
    GroupManager->>Request: 입고 확인
    Request->>Asset: 수량·위치 반영
    Asset->>Asset: OWNED / ACTIVE
    Request->>Request: RECEIVED
~~~

## 9. 요청 상태 모델

~~~mermaid
stateDiagram-v2
    state "대여" as Loan {
        [*] --> PENDING
        PENDING --> ACTIVE: 관리자 승인
        PENDING --> REJECTED: 관리자 반려
        ACTIVE --> RETURN_REQUESTED: 구성원 반납 요청
        RETURN_REQUESTED --> RETURNED: 관리자 실물 확인
    }

    state "도입" as Purchase {
        [*] --> PENDING2
        PENDING2 --> GROUP_APPROVED: 그룹 승인
        PENDING2 --> REJECTED2: 그룹 반려
        GROUP_APPROVED --> BUDGET_APPROVED: 예산 승인 이벤트
        GROUP_APPROVED --> REJECTED2: 예산 반려 이벤트
        BUDGET_APPROVED --> RECEIVED: 입고 확인
    }
~~~

## 10. ERD

~~~mermaid
erDiagram
    USERS {
        bigint id PK
        varchar email UK
        varchar name
        varchar role
    }
    CAMPUS_GROUPS {
        bigint id PK
        varchar name
        varchar slug UK
        varchar invite_code UK
        bigint created_by
        varchar status
    }
    GROUP_MEMBERSHIPS {
        bigint id PK
        bigint group_id
        bigint user_id
        varchar role
        varchar status
    }
    COURSES {
        bigint id PK
        varchar title
        varchar category
        decimal price
        varchar item_type
        int total_quantity
        int available_quantity
        bigint owner_group_id
        varchar visibility
        varchar pickup_location
        int max_loan_days
        bigint instructor_id
        varchar status
        bigint version
    }
    ENROLLMENTS {
        bigint id PK
        bigint user_id
        bigint course_id
        bigint group_id
        varchar request_type
        date requested_from
        date due_date
        varchar reason
        varchar review_comment
        bigint reviewed_by
        varchar status
    }
    PAYMENTS {
        bigint id PK
        bigint user_id
        bigint course_id
        bigint request_id
        bigint group_id
        decimal amount
        varchar status
        varchar transaction_id
    }
    ANALYTICS_LOAN_EVENTS {
        varchar event_id PK
        datetime event_time
        varchar event_type
        bigint request_id
        bigint group_id
        bigint asset_id
        varchar category
        int quantity
        int loan_days
        varchar source
    }
    ANALYTICS_FORECAST_RUNS {
        bigint id PK
        datetime trained_at
        varchar model_name
        decimal baseline_wape
        decimal model_wape
        int train_rows
        int test_rows
    }
    ANALYTICS_FORECASTS {
        bigint id PK
        bigint run_id
        bigint group_id
        varchar category
        date week_start
        decimal predicted_demand
        decimal average_loan_days
    }

    USERS ||--o{ GROUP_MEMBERSHIPS : joins
    CAMPUS_GROUPS ||--o{ GROUP_MEMBERSHIPS : contains
    CAMPUS_GROUPS ||--o{ COURSES : owns
    USERS ||--o{ ENROLLMENTS : requests
    CAMPUS_GROUPS ||--o{ ENROLLMENTS : scopes
    COURSES ||--o{ ENROLLMENTS : targets
    ENROLLMENTS ||--o| PAYMENTS : budget_review
    ANALYTICS_FORECAST_RUNS ||--o{ ANALYTICS_FORECASTS : produces
~~~

ERD의 그룹·분석 관계 일부는 논리 관계다. 실습 환경에서는 서비스 간 결합을 줄이기 위해 모든 ID에 물리 FK를 걸지 않았다.

## 11. 데이터 일관성과 실패 설계

### 현재 보장

- Asset의 borrowOne은 가용 수량 0 이하를 거부한다.
- Asset은 PESSIMISTIC_WRITE 행 잠금과 JPA version으로 동시 수정을 보호한다.
- 반납 확인은 RETURN_REQUESTED에서만 가능하다.
- 입고는 BUDGET_APPROVED 요청과 PURCHASE_REQUEST 자산에만 가능하다.
- 멤버십은 groupId와 userId 조합을 유일하게 유지한다.
- Analytics 이벤트는 eventId 기본키로 멱등 저장한다.

### 현재 한계

Request의 로컬 트랜잭션과 Asset·Budget 원격 호출을 하나의 원자적 트랜잭션으로 묶지 않는다. 원격 호출 성공 뒤 로컬 저장이 실패하면 상태 차이가 생길 수 있다. 운영 제품에서는 Outbox, Saga, 재시도 정책, 멱등 명령, 정합성 점검 Job이 필요하다.

Kafka는 단일 브로커 실습 구성이다. 운영에서는 복제, DLQ, 재처리 도구, 소비 지연 모니터링을 추가해야 한다.

## 12. 보안 경계

- 브라우저 요청은 Gateway에서 Bearer 토큰을 검증한다.
- Gateway가 사용자 ID와 역할 헤더를 각 서비스에 전달한다.
- 서비스는 그룹 멤버십과 관리자 여부를 Member에 재확인한다.
- 학교 예산 API는 INSTRUCTOR 역할을 요구한다.
- 내부 API와 서비스 포트가 로컬에 공개되어 있으므로 현재 구성은 교육용이다.

제품화 시 내부 서비스 인증, 네트워크 차단, 고객사별 테넌트 키, 감사로그, OAuth2 PKCE 또는 BFF를 추가해야 한다.
