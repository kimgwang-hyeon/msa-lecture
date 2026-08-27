# GearHub Campus Product Backlog

## 1. 우선순위 원칙

Backlog는 다음 순서로 정렬한다.

1. 대여·반납 재고 정합성과 권한
2. 그룹별 실제 사용자 흐름
3. 도입·예산·입고의 종단 간 연결
4. 관리자 의사결정에 쓰이는 수요예측
5. 재현 가능한 실행·검증

Must는 현재 MVP의 필수 흐름, Should는 데모와 운영 품질, Could는 후속 확장을 뜻한다. Story Point는 최종 범위의 상대 크기이며 실제 투입 시간을 의미하지 않는다.

## 2. 완료된 MVP Backlog

| ID | Epic | User Story 요약 | 우선순위 | SP | Sprint | 상태 |
|---|---|---|---|---:|---|---|
| F-01 | Foundation | OAuth2 로그인 후 사용자 정보를 확인한다 | Must | 3 | S1 | DONE |
| G-01 | Group | 가입 그룹 목록과 그룹 대시보드를 본다 | Must | 5 | S1 | DONE |
| G-02 | Group | 초대코드로 가입하고 그룹 역할을 관리한다 | Must | 5 | S1 | DONE |
| A-01 | Asset | 학교 공용·그룹 전용 자산을 권한에 맞게 본다 | Must | 5 | S1 | DONE |
| A-02 | Asset | 관리자가 수량·위치·대여일을 포함해 자산을 등록한다 | Must | 5 | S1 | DONE |
| L-01 | Loan | 구성원이 기간과 목적을 입력해 대여를 신청한다 | Must | 5 | S1 | DONE |
| L-02 | Loan | 관리자가 승인·반려하고 승인 시 재고를 차감한다 | Must | 8 | S1 | DONE |
| L-03 | Return | 구성원이 반납 요청하고 관리자가 확인해 재고를 복구한다 | Must | 8 | S1 | DONE |
| L-04 | Loan | 내 요청 상태와 연체 여부를 확인한다 | Should | 3 | S1 | DONE |
| Q-01 | Acquisition | 구성원이 미보유 장비 도입을 요청한다 | Must | 5 | S2 | DONE |
| Q-02 | Acquisition | 그룹 관리자가 필요성을 승인·반려한다 | Must | 5 | S2 | DONE |
| Q-03 | Budget | 학교가 예산을 검토하고 Kafka로 결과를 전달한다 | Must | 8 | S2 | DONE |
| Q-04 | Intake | 관리자가 입고를 확정해 대여 가능 자산으로 전환한다 | Must | 5 | S2 | DONE |
| D-01 | Data | 대여 생명주기 이벤트를 LIVE 분석 이력으로 저장한다 | Must | 5 | S2 | DONE |
| D-02 | Data | 78주 시뮬레이션 이력을 운영 화면과 분리한다 | Must | 5 | S2 | DONE |
| D-03 | AI | 시간순 분리로 기준선과 후보 모델을 비교한다 | Must | 8 | S2 | DONE |
| D-04 | AI | 다음 4주 부족 수량과 그룹 간 이동안을 계산한다 | Must | 8 | S2 | DONE |
| D-05 | AI | 관리자가 평가값과 예측 결과를 화면에서 본다 | Must | 5 | S2 | DONE |
| O-01 | Delivery | 프론트엔드를 포함한 전체 시스템을 Compose로 실행한다 | Must | 5 | S2 | DONE |
| O-02 | Delivery | 고정 시드로 데모 데이터와 계정을 재생성한다 | Should | 5 | S2 | DONE |
| O-03 | Quality | 서비스 테스트, 프론트 빌드, 브라우저 흐름을 검증한다 | Must | 5 | S2 | DONE |

## 3. Sprint별 범위

| Sprint | 목표 | Story | 총 SP |
|---|---|---|---:|
| Sprint 1 | 그룹 안에서 자산을 대여하고 반납해 재고가 원복되는 수직 흐름 | F-01, G-01~02, A-01~02, L-01~04 | 47 |
| Sprint 2 | 장비 도입과 관리자 수요예측을 실제 서비스·데이터로 연결 | Q-01~04, D-01~05, O-01~03 | 69 |

Story Point 합계는 완성된 제품 범위를 설명하는 상대 추정치다. 팀 Velocity 근거로 사용하려면 실제 Sprint 시작·종료 시점의 계획 SP와 Done SP를 별도로 기록해야 한다.

## 4. 핵심 인수 조건

### AC-01 그룹 접근

~~~gherkin
Given 사용자가 로그인했고 특정 그룹의 멤버일 때
When 그룹 워크스페이스를 연다
Then 학교 공용 자산과 해당 그룹 자산을 볼 수 있다
And 다른 그룹 전용 자산은 볼 수 없다
~~~

### AC-02 대여 신청

~~~gherkin
Given 가용 수량이 1 이상인 자산이 있을 때
When 구성원이 그룹, 시작일, 반납 예정일, 사용 목적을 입력한다
Then LOAN 요청이 PENDING으로 생성된다
And 반납 예정일은 시작일보다 빠를 수 없다
And 기간은 자산의 최대 대여일을 넘을 수 없다
~~~

### AC-03 승인과 재고

~~~gherkin
Given PENDING 대여 요청과 가용 재고가 있을 때
When 그룹 관리자가 승인한다
Then 요청은 ACTIVE가 된다
And 가용 수량은 정확히 1 감소한다

Given 가용 재고가 0일 때
When 관리자가 승인을 시도한다
Then 승인은 실패한다
And 재고는 음수가 되지 않는다
~~~

### AC-04 반납

~~~gherkin
Given ACTIVE 대여가 있을 때
When 구성원이 반납을 요청한다
Then 상태는 RETURN_REQUESTED가 된다
And 가용 수량은 변하지 않는다

When 그룹 관리자가 실물 반납을 확인한다
Then 상태는 RETURNED가 된다
And 가용 수량은 정확히 1 증가한다
~~~

### AC-05 도입과 예산

~~~gherkin
Given 구성원이 유효한 장비·수량·가격·링크·사유를 입력할 때
When 도입 요청을 제출한다
Then PURCHASE 요청과 PURCHASE_REQUEST 자산이 생성된다

Given 그룹 관리자가 요청을 승인할 때
Then 상태는 GROUP_APPROVED가 된다
And Budget 검토가 PENDING으로 생성된다

Given 학교 관리자가 예산을 승인할 때
Then payment.completed 이벤트가 발행된다
And Request 상태는 BUDGET_APPROVED가 된다
~~~

### AC-06 입고

~~~gherkin
Given BUDGET_APPROVED 도입 요청이 있을 때
When 그룹 관리자가 입고 수량과 위치를 확인한다
Then 요청은 RECEIVED가 된다
And 자산은 OWNED와 ACTIVE가 된다
And 총수량과 가용수량에 입고 수량이 반영된다
~~~

### AC-07 AI 평가

~~~gherkin
Given 시간순으로 정렬된 주간 대여 데이터가 있을 때
When 모델을 학습한다
Then 최근 4주 이동평균 기준선과 후보 모델을 동일 테스트 구간에서 비교한다
And 선택 모델, MAE, WAPE, 데이터 기간과 행 수를 저장한다
~~~

### AC-08 관리자 예측

~~~gherkin
Given 학습된 모델과 현재 자산 재고가 있을 때
When 관리자가 그룹 분석 화면을 연다
Then 그룹 × 카테고리별 다음 4주 예측을 본다
And 필요 수량, 부족 수량, 위험도, 이동 제안을 함께 본다
~~~

### AC-09 데이터 분리

~~~gherkin
Given 운영 중 수집된 LIVE 이벤트가 있을 때
When 데모 시드를 다시 실행한다
Then SIMULATION 이벤트만 교체된다
And LIVE 이벤트는 보존된다
And 운영 대여 화면에는 제한된 데모 요청만 표시된다
~~~

## 5. Definition of Ready

Story는 다음을 만족할 때 Sprint에 넣을 수 있다.

- 사용자와 기대 가치가 한 문장으로 명확하다.
- 정상·오류 인수 조건이 있다.
- 서비스 소유권과 필요한 API 또는 이벤트가 정해졌다.
- 권한, 상태 전이, 재고 영향이 확인됐다.
- 테스트 데이터와 검증 방법을 설명할 수 있다.
- 외부 의존성과 위험이 드러나 있다.

## 6. Definition of Done

Story는 다음을 모두 만족해야 DONE이다.

- 인수 조건을 코드로 구현했다.
- 권한과 잘못된 상태 전이를 거부한다.
- 재고·요청·예산 데이터 정합성을 확인했다.
- 관련 자동 테스트 또는 재현 가능한 통합 검증이 통과했다.
- Docker 환경에서 실제 흐름을 실행했다.
- API, 아키텍처, 데모 문서를 현재 코드와 맞췄다.
- 합성 데이터나 보안상 한계를 숨기지 않고 기록했다.

## 7. 후속 Product Backlog

| ID | User Story | 우선순위 | 상태 |
|---|---|---|---|
| N-01 | 여러 대학의 데이터와 정책을 완전히 격리한다 | Should | FUTURE |
| N-02 | 날짜가 겹치는 미래 예약의 가용 수량을 계산한다 | Must | FUTURE |
| N-03 | 자산 시리얼별 손상·수리·분실 상태를 관리한다 | Should | FUTURE |
| N-04 | 승인·반납·연체 알림과 SLA를 제공한다 | Should | FUTURE |
| N-05 | 학사·행사 일정을 예측 특징으로 사용한다 | Could | FUTURE |
| N-06 | 모델 드리프트 감지와 정기 재학습을 자동화한다 | Should | FUTURE |
| N-07 | 내부 서비스 인증과 감사로그를 제품 수준으로 강화한다 | Must | FUTURE |
| N-08 | 브라우저 OAuth를 PKCE 또는 BFF 구조로 전환한다 | Must | FUTURE |

## 8. 변경 관리 규칙

- 새 요구는 기존 Sprint Goal을 깨지 않는지 먼저 확인한다.
- 재고와 권한에 영향을 주는 변경은 정상 흐름보다 실패 조건을 먼저 작성한다.
- AI 변경은 모델 이름보다 관리자 행동과 비교 기준을 먼저 정의한다.
- 완료 문서의 수치는 [최종 검증 기록](./10_FINAL_VALIDATION.md)과 함께 갱신한다.
