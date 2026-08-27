# GearHub Campus Agile 실행 기록

## 1. 기록 원칙

이 문서는 현재 제품 범위에서 확인 가능한 의사결정과 산출물을 연결한다. 실제 팀원이 말하지 않은 회의 발언이나 고객 피드백은 만들어 쓰지 않는다. 그런 항목은 TEAM INPUT으로 표시한다.

Product Goal:

> 학교와 소속 그룹의 장비를 대여·반납·도입하는 흐름을 닫고, 관리자가 다음 4주 재고를 데이터로 준비하게 한다.

## 2. Release와 Sprint Goal

| 단계 | Goal | 제품 Increment |
|---|---|---|
| Sprint 1 | 그룹에서 대여와 반납을 완료해 재고가 원복된다 | 그룹·자산·대여·반납 수직 흐름 |
| Sprint 2 | 도입과 수요예측을 관리자 의사결정에 연결한다 | 예산·입고·분석·AI·전체 Compose |
| Release | 재현 가능한 대학 멀티그룹 자산 운영 MVP | 11개 컨테이너, 시드, 테스트, 데모 |

## 3. 제품 의사결정 로그

| ID | 결정 | 근거 | 구현 결과 |
|---|---|---|---|
| D-01 | 고객 단위를 한 학교로 제한 | 이틀 MVP에서 완전한 다학교 격리는 과도함 | 한 학교·8개 그룹 시드 |
| D-02 | 그룹별 서버 대신 동적 URL 사용 | 그룹 추가 시 배포 없이 확장 | /groups/{groupId}/... |
| D-03 | 학교 공용과 그룹 전용 자산을 함께 지원 | 대학의 실제 소유 구조 반영 | ownerGroupId, visibility |
| D-04 | 반납을 구성원 요청과 관리자 확인으로 분리 | 실물 확인 전 재고 복구 방지 | RETURN_REQUESTED, RETURNED |
| D-05 | 도입을 그룹 검토와 학교 예산 검토로 분리 | 필요성과 예산 책임이 다름 | GROUP_APPROVED, BUDGET_APPROVED |
| D-06 | 개인 추천 대신 관리자 수요예측 | 대여자는 필요한 품목을 이미 아는 경우가 많음 | 4주 그룹 × 카테고리 예측 |
| D-07 | 외부 AI API를 사용하지 않음 | 자체 데이터 분석과 평가 근거가 필요 | scikit-learn 후보 모델 |
| D-08 | 운영과 분석 시뮬레이션 데이터를 분리 | 10,814건을 UI에 노출하지 않기 위함 | enrollments와 analytics 이벤트 분리 |
| D-09 | 학교 공용 재고를 그룹별로 중복 계산하지 않음 | 같은 공용 장비를 모든 그룹이 전량 보유한 것으로 볼 수 없음 | 수요 비례 배분 |

## 4. Sprint Board

### Sprint 1

| 상태 | Story |
|---|---|
| DONE | F-01, G-01, G-02 |
| DONE | A-01, A-02 |
| DONE | L-01, L-02, L-03, L-04 |
| 남은 작업 | 없음 |

### Sprint 2

| 상태 | Story |
|---|---|
| DONE | Q-01, Q-02, Q-03, Q-04 |
| DONE | D-01, D-02, D-03, D-04, D-05 |
| DONE | O-01, O-02, O-03 |
| 남은 작업 | 화면 캡처는 제출 준비 항목 |

상세 Story와 인수 조건은 [03_PRODUCT_BACKLOG.md](./03_PRODUCT_BACKLOG.md)에 있다.

## 5. 산출물 기반 실행 기록

| 실행 항목 | 확인 가능한 산출물 |
|---|---|
| 그룹 경계 | campus_groups, group_memberships, 그룹 API·화면 |
| 대여·반납 | Request 상태 전이, Asset 재고 메서드, 통합 검증 |
| 도입·예산 | PURCHASE 요청, Budget, payment.completed |
| 분석 이벤트 | rental.lifecycle Consumer와 analytics_loan_events |
| 모델 | forecast.py의 후보 비교·시간순 분리 |
| 관리자 행동 | Analytics 화면의 shortageUnits와 transferSuggestions |
| 배포 | docker-compose.yml의 11개 서비스 |
| 재현성 | seed_demo_data.py와 데모 계정 |
| 품질 | Spring 테스트, pytest, Vue build와 audit |

## 6. Sprint Review 결과

### Sprint 1 Review

확인된 Increment:

- 로그인 후 가입 그룹 목록과 그룹별 대시보드가 열린다.
- 학교 공용 자산과 현재 그룹 전용 자산을 함께 조회한다.
- 기간·사유를 포함한 대여 요청이 PENDING으로 생성된다.
- 관리자 승인 시 ACTIVE가 되고 재고가 1 감소한다.
- 구성원 반납 요청 시 재고는 유지된다.
- 관리자 반납 확인 시 RETURNED가 되고 재고가 1 복구된다.

Acceptance: Sprint Goal 충족.

### Sprint 2 Review

확인된 Increment:

- 도입 요청이 그룹 검토, 학교 예산 검토, 입고로 이어진다.
- Budget의 결과가 Kafka를 통해 Request에 반영된다.
- 대여 생명주기 이벤트가 LIVE 분석 이력으로 저장된다.
- 78주 이력으로 후보 모델을 비교하고 기준선보다 낮은 WAPE를 얻는다.
- 관리자 화면에서 4주 수요, 필요 수량, 부족, 이동 제안을 본다.
- 프론트엔드를 포함한 11개 컨테이너를 한 번에 실행한다.

Acceptance: Sprint Goal 충족.

### 이해관계자 피드백

TEAM INPUT:

| 질문 | 실제 답변 |
|---|---|
| 가장 가치 있는 흐름은 무엇이었는가 | 입력 필요 |
| 불필요하거나 이해하기 어려운 기능은 무엇인가 | 입력 필요 |
| AI 결과로 실제 어떤 결정을 내릴 수 있는가 | 입력 필요 |
| 다음 Sprint 최우선 개선은 무엇인가 | 입력 필요 |

## 7. 계획 대비 Gap

| 항목 | 현재 결과 | Gap |
|---|---|---|
| 대여 생명주기 | 반납 확인·재고 복구까지 완료 | 미래 예약 중첩 수량 없음 |
| 도입 생명주기 | 그룹·예산·입고 완료 | 발주·배송 연동 없음 |
| 멀티그룹 | 동적 URL과 멤버십 완료 | 다학교 테넌시 없음 |
| AI | 기준선 비교·4주 예측·이동안 완료 | 합성 이력 기반 |
| 운영 품질 | Compose·테스트·시드 완료 | 분산 보상·관찰성 부족 |
| 제출 증거 | 문서와 실행 검증 완료 | 화면 이미지 추가 필요 |

## 8. 근거 기반 Retrospective

### Keep

- 사용자가 끝낼 수 있는 대여·반납 수직 흐름으로 Sprint를 정의한 점
- 그룹별 배포 대신 groupId 기반 워크스페이스를 선택한 점
- AI 평가에 기준선, 시간순 분리, 오차 지표를 함께 둔 점
- 운영 화면과 분석용 대량 이력을 분리한 점
- 고정 시드와 Docker Compose로 데모를 재현한 점

### Problem

- 물리 이름 course, enrollment, payment가 업무 용어와 달라 처음 읽는 사람이 혼동할 수 있다.
- 서비스 간 원격 호출과 로컬 트랜잭션 사이의 보상 처리가 없다.
- 합성 데이터 성능을 실제 운영 효과로 일반화할 수 없다.
- 화면 증빙 파일은 아직 저장소에 추가되지 않았다.

### Try

| 액션 | 다음 확인 기준 | 상태 |
|---|---|---|
| 코드와 발표에서 업무 이름을 우선 사용하고 물리 이름 매핑표 유지 | 신규 팀원이 흐름을 설명 가능 | READY |
| 재고 명령에 idempotency key와 Outbox·Saga Spike | 실패 재현과 보상 설계 | FUTURE |
| 실제 운영 이벤트 비중과 월별 WAPE를 추적 | 모델 재학습 판단 가능 | FUTURE |
| 요청 전후 화면 캡처 추가 | 12개 증빙 목록 완료 | TEAM INPUT |

실제 팀 회고의 Keep·Problem·Try와 담당자는 TEAM INPUT으로 보완한다.

## 9. Agile에서 확인한 배운 점

- 계획을 지키는 것보다 사용자가 얻는 가치를 기준으로 Backlog를 다시 정제하는 것이 중요하다.
- Story를 서비스별 작업으로만 쪼개면 통합이 늦어진다. 사용자 흐름 단위의 수직 Slice가 Review에 유리하다.
- Done은 코딩 완료가 아니라 상태·권한·재고·실행·문서까지 검증된 상태다.
- AI Story도 데이터 생성, 기준선, 평가, API, 관리자 행동을 하나의 Increment로 묶어야 한다.
- MSA 학습의 핵심은 서비스 수가 아니라 책임, 데이터 소유권, 실패 경계를 설명하는 데 있다.

## 10. 추적성

| 평가 관점 | 문서 |
|---|---|
| 문제와 사용자 가치 | [02_PRODUCT_AND_DOMAIN.md](./02_PRODUCT_AND_DOMAIN.md) |
| Backlog와 인수 조건 | [03_PRODUCT_BACKLOG.md](./03_PRODUCT_BACKLOG.md) |
| Sprint 운영 | [04_TWO_DAY_SPRINT_PLAN.md](./04_TWO_DAY_SPRINT_PLAN.md) |
| MSA 설계 | [05_ARCHITECTURE_AND_ERD.md](./05_ARCHITECTURE_AND_ERD.md) |
| API 계약 | [06_API_CONTRACT.md](./06_API_CONTRACT.md) |
| 데모 | [07_VALIDATION_AND_DEMO.md](./07_VALIDATION_AND_DEMO.md) |
| AI 분석 | [09_CAMPUS_PIVOT_AND_AI.md](./09_CAMPUS_PIVOT_AND_AI.md) |
| 실행 증거 | [10_FINAL_VALIDATION.md](./10_FINAL_VALIDATION.md) |
