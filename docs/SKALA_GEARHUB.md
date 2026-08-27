# GearHub Campus 서비스 설명

GearHub Campus는 대학교 안의 학과·연구실·동아리가 학교 공용 장비와 그룹 전용 장비를 한 시스템에서 대여·반납·도입하는 B2B 자산 운영 MVP다. 기존 Agile MSA 실습 템플릿을 기반으로 서비스 경계와 주요 물리 이름을 유지하면서 실제 업무 흐름을 확장했다.

## 해결하려는 문제

- 어떤 장비가 학교 공용이고 어떤 장비가 그룹 소유인지 한눈에 알기 어렵다.
- 신청과 승인만 기록하고 반납·재고 복구가 분리되어 있다.
- 그룹에 없는 장비를 도입할 때 필요성 검토와 학교 예산 검토의 책임이 모호하다.
- 관리자는 미래 수요를 감으로 판단해 장비가 남거나 필요한 시기에 부족해진다.

## 사용자 가치

- 구성원은 소속 그룹 화면에서 조회 가능한 모든 자산과 실제 가용 수량을 본다.
- 그룹 관리자는 대여 승인, 반납 확인, 도입 1차 검토, 입고를 처리한다.
- 학교 관리자는 그룹 전체의 예산과 다음 4주 수요를 확인한다.
- 부족한 장비는 바로 구매하라는 결론 대신 여유 그룹의 이동 가능 수량을 먼저 검토한다.

## 서비스와 데이터 소유권

| 서비스 | 논리 책임 | 주요 소유 테이블 |
|---|---|---|
| Member Service | 사용자, 그룹, 멤버십, 그룹 역할 | `users`, `campus_groups`, `group_memberships` |
| Asset Service | 학교·그룹 자산, 수량 재고, 입고 | `courses` |
| Request Service | 대여·반납·도입 상태 머신 | `enrollments` |
| Budget Service | 학교 예산 승인·반려 | `payments` |
| Demand Analytics Service | 대여 이벤트, 모델 실행, 4주 예측 | `analytics_loan_events`, `analytics_forecast_runs`, `analytics_forecasts` |

Auth Server, API Gateway, Eureka, Kafka, MariaDB는 공통 인프라다. 프론트는 모든 사용자 API를 Gateway로 호출한다.

## 주요 흐름

### 대여

```text
자산 조회 → 기간·사유 입력 → PENDING
→ 그룹 관리자 승인 → ACTIVE + 재고 -1
```

### 반납

```text
ACTIVE → 구성원 반납 요청 → RETURN_REQUESTED
→ 관리자 실물 확인 → RETURNED + 재고 +1
```

### 도입

```text
미보유 장비 요청 PENDING
→ 그룹 승인 GROUP_APPROVED
→ 학교 예산 승인 BUDGET_APPROVED
→ 입고 RECEIVED
→ 대여 가능한 OWNED 자산
```

Budget 결과는 Kafka 이벤트로 Request Service에 반영된다. 대여 라이프사이클도 Kafka로 Analytics Service에 전달되어 `LIVE` 이력으로 축적된다.

## 그룹 경계

한 그룹마다 별도 배포를 만들지 않고 `/groups/{groupId}/...` 동적 라우트를 사용한다.

- `ORGANIZATION` 자산: 모든 그룹이 조회할 수 있는 학교 공용 자산
- `GROUP` 자산: 해당 그룹 구성원만 조회할 수 있는 자산
- `MEMBER`: 조회·신청·반납 요청
- `MANAGER`: 그룹 승인·반려·입고·구성원 관리
- `INSTRUCTOR`: 학교 관리자이며 모든 그룹에서 관리자 권한

## 관리자용 AI

AI 문제는 개인 장비 추천이 아니라 다음 4주 `그룹 × 카테고리` 대여 수요 예측으로 정의했다.

1. 78주 요청 이력을 주 단위로 집계한다.
2. lag, 4주 이동통계, 주차 순환 특징을 만든다.
3. 시간순으로 학습·검증·테스트를 분리한다.
4. 최근 4주 이동평균과 scikit-learn 후보 모델을 같은 테스트 구간에서 비교한다.
5. 예측 수요, 평균 대여기간, 현재 재고를 결합해 필요 수량을 계산한다.
6. 부족할 때 여유 그룹의 이동 가능 수량을 먼저 제안한다.

고정 시드 최종 결과는 Histogram Gradient Boosting, 기준선 WAPE 70.3414%, 모델 WAPE 55.7159%, 개선율 20.79%다. 이 값은 합성 이력으로 분석 파이프라인과 비교 방법을 검증한 것이며 실제 운영 정확도를 보장하지 않는다.

## 주요 공개 API

| 기능 | API |
|---|---|
| 내 그룹 | `GET /api/users/groups/my` |
| 초대코드 참여 | `POST /api/users/groups/join` |
| 그룹 자산 | `GET /api/courses?groupId={id}` |
| 대여 신청 | `POST /api/enrollments` |
| 그룹 요청 조회 | `GET /api/enrollments/group/{groupId}` |
| 대여 승인 | `POST /api/enrollments/{id}/approve` |
| 반납 요청·확인 | `POST /api/enrollments/{id}/return-request`, `return-confirm` |
| 도입 요청 | `POST /api/enrollments/acquisitions` |
| 그룹 도입 승인 | `POST /api/enrollments/{id}/group-approve` |
| 예산 승인·반려 | `POST /api/payments/{id}/approve`, `reject` |
| 입고 | `POST /api/enrollments/{id}/receive` |
| 모델 평가 | `GET /api/recommend/analytics/evaluation` |
| 그룹 4주 예측 | `GET /api/recommend/analytics/forecast?groupId={id}` |

## 실행

```powershell
docker load -i infra-images.tar
docker compose up -d --build
docker compose exec -T alternative-service python scripts/seed_demo_data.py
```

- Frontend: <http://localhost:3000>
- Gateway: <http://localhost:8080>
- Eureka: <http://localhost:8761>

데모 계정과 검증 명령은 루트 [readme.md](../readme.md), 제품 전환 근거는 [09_CAMPUS_PIVOT_AND_AI.md](./agile/09_CAMPUS_PIVOT_AND_AI.md), 실행 증거는 [10_FINAL_VALIDATION.md](./agile/10_FINAL_VALIDATION.md)를 참고한다.
