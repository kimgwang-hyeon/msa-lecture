# 코드 템플릿 이해 서브노트

## 1. 실습 코드의 현재 해석

GearHub Campus는 제공된 강의·수강·결제·추천 MSA의 실행 구조를 유지하면서 업무 의미를 대학 자산 운영으로 바꾼 결과다. 폴더명과 일부 API prefix는 호환성을 위해 남아 있지만 사용자 화면과 서비스 책임은 현재 도메인을 따른다.

이 문서에서 물리 이름은 코드와 배포 단위를, 업무 이름은 제품이 책임지는 기능을 뜻한다.

## 2. 물리 구조와 업무 의미

| 코드 폴더 | 등록 서비스 | 현재 업무 이름 | 책임 |
|---|---|---|---|
| user-service | member-service | Member | 사용자, 그룹, 멤버십, 역할 |
| course-service | asset-service | Asset | 자산, 수량, 공개 범위, 입고 |
| enrollment-service | request-service | Request | 대여·반납·도입 요청과 상태 전이 |
| payment-service | budget-service | Budget | 학교 예산 검토 |
| recommend-service | alternative-service | Demand Analytics | 대여 이벤트, 모델 학습·평가, 수요예측 |
| vue-frontend | gearhub-frontend | Web | 그룹 워크스페이스와 사용자 흐름 |
| eureka-server | eureka-server | Discovery | 서비스 등록과 탐색 |
| docker-compose의 gateway | api-gateway | Gateway | OAuth2 검증, 사용자 헤더 전달, 라우팅 |
| docker-compose의 auth | auth-server | Auth | 로그인과 토큰 발급 |

MariaDB와 Kafka는 공통 인프라다. 서비스들은 같은 MariaDB 인스턴스를 사용하지만 테이블 책임은 서비스별로 분리한다.

## 3. 현재 사용자 흐름

### 대여·반납

~~~text
Web
→ Gateway
→ Request: 대여 요청 PENDING
→ Member: 그룹 접근 권한 확인
→ Asset: 자산과 가용 수량 확인
→ Request: 관리자 승인 ACTIVE
→ Asset: availableQuantity 1 감소
→ Request: 구성원 반납 요청 RETURN_REQUESTED
→ Request: 관리자 반납 확인 RETURNED
→ Asset: availableQuantity 1 복구
~~~

### 도입·예산·입고

~~~text
Web
→ Request: 도입 요청 PENDING
→ Asset: PURCHASE_REQUEST 자산 생성
→ Request: 그룹 승인 GROUP_APPROVED
→ Budget: 예산 검토 PENDING
→ Budget: 승인 COMPLETED
→ Kafka payment.completed
→ Request: BUDGET_APPROVED
→ Request: 입고 RECEIVED
→ Asset: OWNED / ACTIVE, 수량 반영
~~~

### 분석

~~~text
Request
→ Kafka rental.lifecycle
→ Demand Analytics: LIVE 이벤트 저장
→ 주 단위 집계와 시간순 학습
→ 다음 4주 수요예측
→ Asset: 현재 그룹·공용 재고 조회
→ 부족 수량과 그룹 간 이동 제안 반환
~~~

## 4. 코드에서 유지한 것과 확장한 것

| 구분 | 유지한 요소 | 현재 확장 |
|---|---|---|
| 실행 | Spring Boot, FastAPI, Vue, Eureka, Gateway, Kafka, MariaDB | 프론트엔드까지 Compose 포함 |
| 라우팅 | courses, enrollments, payments, recommend prefix | 그룹 중심 동적 URL과 권한 범위 |
| 데이터 | users, courses, enrollments, payments | 그룹·멤버십, 재고, 반납, 분석 테이블 |
| 이벤트 | payment.completed, enrollment.completed 호환 | rental.lifecycle 분석 이벤트 |
| 인증 | OAuth2 로그인과 JWT | Gateway가 X-User-Id를 내부 요청에 전달 |
| 추천 서비스 | Python 서비스와 내부 클라이언트 | scikit-learn 기반 관리자 수요예측 |

enrollment.completed와 대체재 API는 기존 계약 호환을 위해 코드에 남아 있다. 현재 제품의 핵심 AI와 데모 흐름은 rental.lifecycle 및 analytics API다.

## 5. 핵심 데이터 확장

### Member 소유

- users: 학교 사용자와 조직 역할 STUDENT, INSTRUCTOR
- campus_groups: 그룹 이름, slug, 설명, 초대코드, 생성자, 상태
- group_memberships: 사용자와 그룹의 MEMBER, MANAGER 역할
- 초대코드는 그룹 관리자 응답에만 포함

### Asset 소유

- courses: 제품에서는 자산
- owner_group_id가 없으면 학교 공용, 값이 있으면 그룹 소유
- visibility: ORGANIZATION 또는 GROUP
- item_type: OWNED 또는 PURCHASE_REQUEST
- total_quantity와 available_quantity로 수량 재고 관리
- pickup_location, max_loan_days, version으로 운영 정보와 동시성 관리

### Request 소유

- enrollments: 제품에서는 대여 또는 도입 요청
- request_type: LOAN 또는 PURCHASE
- group_id, reason, review_comment
- requested_from, due_date
- approved_at, return_requested_at, returned_at, reviewed_by
- 상태 전이와 overdue 계산

### Budget 소유

- payments: 실제 카드 결제가 아니라 예산 검토 기록
- request_id와 group_id로 도입 요청을 추적
- PENDING, COMPLETED, FAILED, CANCELLED 상태

### Demand Analytics 소유

- analytics_loan_events: SIMULATION 및 LIVE 대여 생명주기 이벤트
- analytics_forecast_runs: 실행 모델과 평가 지표
- analytics_forecasts: 그룹·카테고리·예측 주차별 결과

## 6. 폴더별 확인 위치

| 확인 목적 | 위치 |
|---|---|
| 그룹·멤버십 | user-service/src/main/java/com/lecture/user |
| 자산·재고 | course-service/src/main/java/com/lecture/course |
| 요청 상태 전이 | enrollment-service/src/main/java/com/lecture/enrollment |
| 예산 이벤트 | payment-service/src/main/java/com/lecture/payment |
| 분석·모델 | recommend-service/app/analytics, recommend-service/app/service |
| 재현 시드 | recommend-service/scripts/seed_demo_data.py |
| 그룹 화면 | vue-frontend/src/views, vue-frontend/src/router |
| 서비스 연결 | docker-compose.yml |
| 초기 스키마 | init-db |

## 7. 서비스 경계를 유지한 이유

- 사용자와 그룹 정책은 Member가 한 곳에서 판단한다.
- 재고 증감은 Asset만 수행해 음수 재고를 방지한다.
- Request는 업무 상태를 조정하지만 다른 서비스 테이블을 직접 수정하지 않는다.
- Budget 결과는 Kafka 이벤트로 전달해 예산 검토와 요청 처리를 분리한다.
- Analytics는 운영 요청 테이블을 직접 소유하지 않고 이벤트를 축적한다.

같은 데이터베이스 인스턴스를 쓰는 것은 실습 운영 단순화를 위한 선택이다. 논리적 소유권까지 공유한다는 뜻은 아니다.

## 8. Docker 실행 이해

Docker Compose는 11개 컨테이너를 실행한다.

| 구성 | 공개 포트 |
|---|---:|
| Web | 3000 |
| Eureka | 8761 |
| Gateway | 8080 |
| Auth | 9000 |
| Member | 8081 |
| Asset | 8082 |
| Request | 8083 |
| Budget | 8084 |
| Demand Analytics | 8085 |
| Kafka | 9092 |
| MariaDB | 3379 |

호스트에서 Spring 테스트를 실행할 때는 MariaDB와 Kafka 주소를 localhost 포트로 덮어써야 한다. 컨테이너 내부에서는 lecturedb와 kafka라는 서비스 이름을 사용한다.

## 9. 이 구조에서 확인한 학습점

- MSA는 서비스 개수보다 데이터 책임과 변경 이유를 나누는 설계가 중요하다.
- 템플릿을 재사용해도 상태와 용어를 명시적으로 바꾸지 않으면 업무 흐름이 어긋난다.
- 대여는 승인에서 끝나지 않고 반납 확인과 재고 복구까지 구현해야 수직 기능이 완성된다.
- 비동기 이벤트는 결합도를 줄이지만 중복 처리, 실패 복구, 관찰 가능성을 함께 설계해야 한다.
- AI는 별도 장식이 아니라 운영자가 취할 행동과 평가 기준을 먼저 정의해야 한다.

## 10. 현재 범위 밖

- 학교 간 완전한 멀티테넌시
- 미래 날짜 구간별 예약 충돌 수량 계산
- 자산 시리얼 단위 손상·수리·분실
- 실제 발주·배송·회계 시스템 연동
- 서비스 간 별도 인증과 완전한 감사로그
- 운영 데이터 기반의 장기 모델 검증과 드리프트 자동 감지
