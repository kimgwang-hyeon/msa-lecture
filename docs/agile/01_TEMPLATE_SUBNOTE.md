# 코드 템플릿 이해 서브노트

## 1. 이번 실습에서 이해할 범위

가이드의 핵심은 인프라 코드를 처음부터 재작성하는 것이 아니라, 제공된 API 계약과 전체 흐름을 이해하고 팀 도메인으로 치환하는 것이다.

### 이해해야 하는 것

- 로그인 후 Access Token이 발급되고 Gateway를 통해 요청한다.
- Gateway가 사용자 ID·이메일·역할을 하위 서비스에 전달한다.
- 각 업무 서비스가 어떤 데이터를 소유하고 어떤 API를 제공하는지 안다.
- 즉시 결과가 필요한 구간은 REST, 상태 변화 알림은 Kafka를 사용한다.
- 예산 승인·반려 이벤트 이후 신청 상태가 자동으로 바뀌는 결과를 확인한다.
- 프론트엔드는 개별 서비스 포트가 아니라 Gateway `:8080`을 호출한다.

### 블랙박스로 사용해도 되는 것

- OAuth2 Authorization Code와 JWK의 내부 구현
- Gateway 필터 체인의 세부 구현
- Eureka의 레지스트리 동기화 알고리즘
- Kafka Producer/Consumer 라이브러리 내부 동작
- JPA가 SQL을 생성하는 세부 과정

## 2. 원본 템플릿 흐름

```text
사용자 로그인
  → 강의 조회
  → 수강신청 생성(PENDING)
  → 결제 처리
  → payment.completed
  → 수강신청 ACTIVE
  → enrollment.completed
  → 추천 갱신
```

## 3. GearHub 치환 결과

```text
교육생 로그인
  → 보유 교보재와 재고 조회
  → 대여 신청(PENDING)
  → 운영진 대여 승인
  → 재고 차감과 신청 ACTIVE

신규 교보재가 필요한 경우
  → 동일 카테고리 보유 대체재 확인
  → 상품 링크·단가·수량·사유 제출
  → 예산 검토(PENDING)
  → 운영진 승인 또는 반려
  → payment.completed 이벤트
  → 구매 신청 ACTIVE 또는 REJECTED
```

## 4. 코드 위치와 새 서비스명

최소 변경 원칙에 따라 소스 디렉터리와 Java 패키지는 유지하고 실행 시 사용하는 논리 서비스명만 변경했다.

| 소스 디렉터리 | GearHub 서비스명 | 재사용한 핵심 클래스 | 역할 |
|---|---|---|---|
| `user-service` | `member-service` | `User`, `UserController` | 교육생·운영진 계정 |
| `course-service` | `asset-service` | `Course`, `CourseController` | 교보재·재고·구매요청 상품 |
| `enrollment-service` | `request-service` | `Enrollment`, `EnrollmentController` | 대여·구매 신청 상태 |
| `payment-service` | `budget-service` | `Payment`, `PaymentController` | 예산 검토 상태 |
| `recommend-service` | `alternative-service` | `RecommendService` | 구매 전 보유 대체재 추천 |

## 5. 최소 수정 전략

- 새 마이크로서비스를 추가하지 않고 기존 다섯 업무 서비스를 재해석했다.
- `courses`, `enrollments`, `payments` 테이블을 유지하고 필요한 컬럼만 추가했다.
- 기존 `/api/courses`, `/api/enrollments`, `/api/payments`, `/api/recommend` 경로를 유지했다.
- 기존 Kafka 토픽 `payment.completed`, `enrollment.completed`를 유지하고 상태 필드를 확장했다.
- Auth Server·Gateway 이미지의 내부 코드는 수정하지 않았다.
- 프론트엔드는 화면과 문구를 크게 바꾸되 기존 Axios·Pinia·Router 구조를 재사용했다.

## 6. 추가·변경한 핵심 데이터

### Asset (`courses`)

- `item_type`: `OWNED` 또는 `PURCHASE_REQUEST`
- `total_quantity`, `available_quantity`: 전체·가용 재고
- `purchase_url`: 신규 구매요청 상품 링크
- `price`: 보유 교보재는 자산가치, 구매요청은 단가

### Request (`enrollments`)

- `request_type`: `LOAN` 또는 `PURCHASE`
- `reason`: 신청 목적
- `review_comment`: 반려 사유
- `status`: `PENDING`, `ACTIVE`, `REJECTED`, `CANCELLED`

### Budget (`payments`)

- `amount`: 서버가 계산한 `단가 × 수량`
- `transaction_id`: 실제 거래번호 대신 예산 승인번호
- `status`: `PENDING`, `COMPLETED`, `FAILED`, `CANCELLED`

## 7. 시행착오와 판단 기록

| 상황 | 확인한 원인 | 대응 | 배운 점 |
|---|---|---|---|
| Spring 전체 Context Test가 로컬에서 실패 | DB 호스트명이 Docker DNS인 `lecturedb`로 설정됨 | 순수 도메인 단위 테스트와 Docker 통합 테스트를 분리 | 환경 의존 테스트와 비즈니스 로직 테스트를 구분해야 함 |
| Docker 전체 빌드 중 기반 이미지 메타데이터 타임아웃 | 외부 Registry 응답 지연 | 소스는 그대로 두고 로컬 JAR·기존 이미지로 통합 검증 | 인프라 장애와 애플리케이션 오류를 분리해 진단해야 함 |
| Vue 빌드가 `fsevents` 네이티브 모듈에서 멈춤 | 현재 macOS 환경의 선택 의존성 로딩 문제 | 빌드 동안만 모듈을 비활성화하고 즉시 복구 | 임시 우회는 범위와 복구 여부를 기록해야 함 |
| Alternative API 직접 호출 시 403 | JWT가 필요한 엔드포인트를 토큰 없이 호출 | Gateway와 로그인 토큰을 통한 호출을 최종 시연 항목으로 유지 | 인증 오류는 추천 로직 실패와 다름 |
| 새 Eureka 서비스명 사용 시 Gateway 호환 우려 | 제공 Gateway 이미지에 기존 `lb://...` 이름이 고정됨 | 이미지를 수정하지 않고 Compose 환경변수로 Route URI를 재정의 | 인프라 계약을 깨지 않고 외부 설정으로 확장할 수 있음 |

## 8. Docker·Compose 이해 점검

보강 2의 핵심은 이미지와 실행 중인 컨테이너를 구분하는 것이다. 소스를 바꾸거나 이미지를 빌드해도 기존 컨테이너가 자동으로 새 내용으로 바뀌지 않으므로, 현재 실행 환경에 이전 서비스명이 남은 것은 모순이 아니다.

| 점검 항목 | 현재 프로젝트 | 판정 |
|---|---|---|
| 서비스별 독립 이미지 | 각 업무 서비스가 별도 Dockerfile·빌드 컨텍스트를 가짐 | 충족 |
| Java 멀티스테이지 | JDK builder에서 JAR 생성 후 JRE runtime으로 복사 | 충족 |
| Frontend 멀티스테이지 | Node builder에서 빌드 후 Nginx로 정적 파일 제공 | 충족 |
| Compose 내부 연결 | 공통 네트워크와 서비스 DNS·환경변수로 연결 | 충족 |
| 호스트·컨테이너 포트 | 각 서비스 포트 매핑이 Compose에 명시됨 | 충족 |
| 실행 계정 최소 권한 | 업무 Dockerfile에 별도 `USER`가 없음 | 제품화 전 보강 |
| 환경변수·Secret 관리 | 설정은 환경변수화됐으나 예시 파일·Secret 저장 정책 정리가 필요 | 제품화 전 보강 |
| 고정 이미지 출처 | 태그 기반이며 digest 고정·이미지 검증 정책은 없음 | 제품화 전 보강 |

이번 Sprint에서는 Dockerfile 보안 개편이나 기반 이미지 변경을 하지 않는다. 현재 이미지 보존 요구와 핵심 사용자 흐름 우선순위를 지키고, 최소 권한·Secret·digest는 별도 기술 Backlog로 관리한다.

## 9. 현재 검증 결과

- Asset·Request·Budget 도메인 단위 테스트: `DONE`
- Python 소스 컴파일 검사: `DONE`
- Vue Production Build: `DONE`
- Docker Compose 설정 검증: `DONE`
- 대여 신청 → 승인 → 재고 2개에서 1개로 감소: `DONE`
- 구매요청 → 예산 승인 → Kafka → 신청 ACTIVE: `DONE`
- 예산 반려 → Kafka → 신청 REJECTED: `DONE`
- 새 서비스명으로 Docker 재기동·Eureka 화면 캡처: `READY`

## 10. 아직 하지 않는 것

- 반납과 재고 복구
- 날짜별 예약 충돌
- 발주·배송·입고
- 알림과 파일 첨부
- 별도 데이터베이스 인스턴스 분리
- 임베딩이나 LLM을 사용한 의미 기반 추천
