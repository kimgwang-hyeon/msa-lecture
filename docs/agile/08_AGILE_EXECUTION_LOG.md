# Agile 실행 로그

## 1. 문서 목적과 기록 원칙

이 문서는 추가 PDF인 `Agile 방법론 및 MSA 개발`에서 강조한 Product Backlog, Sprint Backlog, Increment, Sprint Board, Velocity, Review, 계획 대비 실적과 Retrospective 액션을 GearHub에 적용한다.

- `CODE EVIDENCE`: 현재 소스·테스트·구성에서 확인한 사실
- `RECONSTRUCTED`: 현재 결과를 기준으로 재구성한 보드·계획 초안이며 당시 회의록은 아님
- `TEAM INPUT`: 팀이 실제 행사에서 이름·발언·피드백·시간을 입력할 항목
- Done SP는 [03_PRODUCT_BACKLOG.md](./03_PRODUCT_BACKLOG.md)의 DoD를 만족한 항목만 센다.
- 속도는 팀 성과 평가가 아니라 다음 계획의 참고값이다. 이틀치 두 점만으로 장기 생산성을 예측하지 않는다.

## 2. Release Roadmap

| Release | 목표 고객·가치 | 포함 범위 | 종료 조건 | 상태 |
|---|---|---|---|---|
| `0.1` / Day 1 | SKALA 구성원이 문의 없이 보유 장비를 빌림 | Member, Asset, Request, 대여 UI | 신청 PENDING → 승인 ACTIVE, 재고 2 → 1 | CODE EVIDENCE / DONE |
| `0.2` / Day 2 | 자산 운영자와 예산 승인자가 신규 구매 근거를 한 흐름에서 검토 | Alternative, Budget, Kafka, 구매·승인 UI | 대체재 확인, 총액 계산, 승인·반려 상태 전파 | CODE EVIDENCE / DONE |
| `0.2-RC` / 데모 전 | 새 GearHub 이름으로 실행 증거 확보 | 새 Eureka 이름, Gateway E2E, 화면 캡처 | 5개 서비스 UP, 로그인 기반 Smoke Test, 캡처 10종 | READY / TEAM INPUT |
| `1.0 Pilot` / Future | 첫 교육기관·기업 고객이 안전하게 사용 | 테넌트 격리, SSO, 권한, 감사로그, 반납 | 고객 조직별 데이터 분리·운영 기준 충족 | FUTURE |

Release는 배포와 같은 말이 아니다. 현재 `0.2` 기능 코드는 검증됐지만 사용자의 요청에 따라 이미지를 다시 만들지 않았으므로, 새 서비스명으로 실제 컨테이너를 띄우는 `0.2-RC`는 아직 완료되지 않았다.

## 3. Sprint 0 결정 기록

| 결정 항목 | 현재 초안 | 확정 주체 |
|---|---|---|
| 제품 한 문장 | 교육기관·기업의 내부 장비 대여·구매요청을 관리하는 B2B SaaS | 사용자 요청으로 확정, 팀 PO 문구 확인 필요 |
| 첫 고객·데모 조직 | SKALA 운영 조직 | 사용자 요청으로 확정 |
| MVP 테넌시 | 한 조직만 지원; 멀티테넌시는 Future | 현재 코드로 확정 |
| Sprint 주기 | 하루 1 Sprint, 총 2 Sprint | 사용자 요청으로 확정 |
| Sprint 1 Goal | 대여 워킹 스켈레톤 | 문서·코드로 확인 |
| Sprint 2 Goal | 대체재·구매요청·예산·Kafka 확장 | 문서·코드로 확인 |
| 제공 인프라 | Auth, Gateway, Eureka 내부 구현 미수정 | 사용자 요청·가이드로 확정 |
| 공통 DoD | 빌드·테스트·계약·정상/오류 흐름·데모 가능 | 팀 최종 확인 필요 |
| 보드 열 | To Do / In Progress / Done | 팀 최종 확인 필요 |

### 역할 순환표

| 역할 | Day 1 | Day 2 | 책임 |
|---|---|---|---|
| Product Owner | `TEAM INPUT` | `TEAM INPUT` — Day 1과 다른 사람 권장 | Sprint Goal·우선순위·수용 여부 결정 |
| Scrum Master | `TEAM INPUT` | `TEAM INPUT` — Day 1과 다른 사람 권장 | Timebox·막힘·보드·행사 진행 |
| Development Team | `TEAM INPUT` | `TEAM INPUT` | 설계·구현·테스트·통합·문서 공동 책임 |

전공별 권장 작업은 [04_TWO_DAY_SPRINT_PLAN.md](./04_TWO_DAY_SPRINT_PLAN.md)를 참고하되, PO·SM은 전공과 무관하게 순환한다.

## 4. Sprint Board — 현재 코드 기준 재구성

이 보드는 실시간 도구에서 내보낸 과거 기록이 아니라 현재 저장소 상태를 바탕으로 만든 `RECONSTRUCTED` 초안이다. 실제 실습 중에는 작업을 시작할 때 `In Progress`, DoD를 만족할 때만 `Done`으로 옮긴다.

### Sprint 1 / Day 1

| To Do | In Progress | Done |
|---|---|---|
| 없음 | 없음 | G-01, M-01~02, A-01~03, R-01~04 |

Sprint 1 Increment: 로그인한 구성원이 보유 장비를 조회·신청하고 운영진 승인 후 재고가 감소하는 동작 흐름.

### Sprint 2 / Day 2

| To Do | In Progress / READY | Done |
|---|---|---|
| D-01 화면 캡처 | N-01 새 이름으로 컨테이너 재기동·Eureka 확인 | X-01~03, B-01~02, E-01, F-01, N-02 |

Sprint 2 Increment: 구매 전에 대체재를 확인하고, 구매요청의 총액과 예산 승인·반려 결과가 Kafka를 통해 Request 상태에 반영되는 흐름.

## 5. 계획 SP와 Done SP

| Sprint | 계획 SP | Done SP | 미완료 SP | 완료율 | 해석 |
|---|---:|---:|---:|---:|---|
| Sprint 1 | 24 | 24 | 0 | 100% | 대여 워킹 스켈레톤 코드·통합 검증 완료 |
| Sprint 2 | 30 | 26 | 4 | 86.7% | 기능은 완료, 실행 이름 확인 2 SP와 캡처 2 SP가 남음 |

Sprint 2의 `N-01`은 소스·Compose 구성까지 준비됐지만 새 이미지·컨테이너에서 확인하지 않아 Done으로 세지 않는다. `D-01`은 실제 화면 파일이 없어 Done이 아니다.

다음 Sprint 용량을 잡아야 한다면 최근 Done SP `24~26`을 참고하되, 팀원 수·가용 시간·기술 불확실성을 함께 보고 결정한다.

## 6. Daily Scrum 기록

Daily Scrum은 15분 안에 Sprint Goal 기준으로 진행한다. 각 팀원은 “어제 한 일”의 장황한 보고보다 `완료 / 다음 / 막힘`과 보드 이동을 말한다.

### 실습 중 입력 양식

| 일시 | 팀원 | 완료 | 다음 | 막힘·도움 요청 | 보드 이동 |
|---|---|---|---|---|---|
| `TEAM INPUT` | `TEAM INPUT` | `TEAM INPUT` | `TEAM INPUT` | `TEAM INPUT` | `TEAM INPUT` |

### 현재 코드에서 확인된 상태 요약

아래는 회의 발언이 아니라 코드·검증 결과를 Daily 형식으로 요약한 `RECONSTRUCTED` 참고 자료다.

| 시점 | 완료 | 다음 | 막힘·결정 |
|---|---|---|---|
| Day 1 중간 | Asset 수량 모델, LOAN PENDING, 승인·반려 상태 전이 | 프론트 연결, 승인 후 수량 확인 | Docker DB 주소 때문에 전체 Context Test 분리 필요 |
| Day 1 종료 | 대여 신청 → 승인 → 재고 2→1 통합 흐름 | 구매 전 대체재와 Budget 설계 | 기존 Course·Enrollment 용어를 외부 설명에서 변환 |
| Day 2 중간 | 구매요청, 서버 총액 계산, Budget PENDING | Kafka 승인·반려, 화면 통합 | Gateway 고정 라우팅과 새 Eureka 이름의 호환 방식 확인 |
| Day 2 종료 | 승인·반려 이벤트와 프론트 빌드 검증 | 새 이름 재기동, Gateway E2E, 캡처 | 이미지 미변경 요청과 Registry 지연 때문에 실행 확인 보류 |

## 7. Sprint Review 기록

### Review 진행 순서

1. Sprint Goal을 다시 읽는다.
2. 실제 동작 Increment만 시연한다.
3. Product Backlog와 완료·미완료 Story를 함께 본다.
4. 이해관계자 피드백을 기능·우선순위·질문으로 기록한다.
5. 미완료 항목은 숨기지 않고 다음 Backlog 위치를 결정한다.

가능하면 팀 밖의 다른 팀원·강사·운영 관점 사용자 중 최소 1명이 실제 Increment를 보고 의견을 주도록 한다. 내부 팀의 추측은 외부 피드백으로 기록하지 않는다.

### 확인된 제품 결정

| 시점 | 결정 | 근거 | 반영 |
|---|---|---|---|
| Sprint 1 이후 | 실제 결제가 아니라 Budget 승인·반려로 해석 | 내부 장비 운영에서 돈을 받는 결제보다 예산 의사결정이 핵심 | B-01~02, E-01 |
| Sprint 1 이후 | 신규 구매 전에 보유 대체재를 먼저 확인 | 중복 구매 감소가 조직 고객 가치 | X-01~03 |
| Sprint 2 문서화 | SKALA 전용 앱이 아니라 교육기관·기업 대상 B2B SaaS로 포지셔닝 | 사용자 요청 | 제품 정의·향후 SaaS Backlog |
| Sprint 2 문서화 | 현재는 단일 테넌트 MVP라고 명시 | 코드에 조직 격리가 없음 | SAAS-01~06 Future |

### 실제 이해관계자 피드백 입력란

| Sprint | 참석자 | 관찰·질문 | PO 결정 | Backlog 반영 |
|---|---|---|---|---|
| Sprint 1 | `TEAM INPUT` | `TEAM INPUT` | `TEAM INPUT` | `TEAM INPUT` |
| Sprint 2 | `TEAM INPUT` | `TEAM INPUT` | `TEAM INPUT` | `TEAM INPUT` |

## 8. 계획 대비 실적과 Gap

| 계획 | 실제 | 차이 원인 | 영향 | 처리 |
|---|---|---|---|---|
| Day 1에 대여 E2E 완성 | 서비스 포트 기준 신청·승인·재고 차감 통과 | 없음 | 핵심 사용자 가치 확보 | Done |
| 전체 Spring Context Test 사용 | DB 의존 Context는 로컬 주소 조건 때문에 즉시 실패 | 테스트 환경과 Docker DB 주소 차이 | 전체 기동 검증과 순수 도메인 검증 분리 필요 | 단위 테스트와 Docker 통합 테스트로 보완 |
| Day 2에 구매·예산·Kafka 확장 | 승인·반려 양쪽 상태 전파 통과 | 없음 | 조직 구매 검토 흐름 확보 | Done |
| 새 서비스명을 실제 Eureka에서 확인 | 소스·Compose만 변경하고 실행 컨테이너는 이전 이름 유지 | 이미지를 건드리지 말라는 사용자 결정 | 데모 캡처에서 새 이름을 아직 증명 못함 | N-01 READY |
| 화면 증거 10종 확보 | 캡처 체크리스트와 폴더만 준비 | 새 이름 재기동·인증 E2E 선행 필요 | 제출 증거 미완성 | D-01 To Do |
| 완성형 B2B SaaS | 단일 조직 핵심 흐름만 구현 | 이틀 MVP와 기존 코드 최소 수정 원칙 | 다고객 운영 불가 | 멀티테넌시·SSO·감사로그 Future |

## 9. Retrospective와 액션 추적

### Sprint 1 Keep / Problem / Try

| 구분 | 내용 | 액션 | 상태 |
|---|---|---|---|
| Keep | 기존 API·테이블을 재사용해 빠르게 수직 흐름을 만들었다. | Day 2도 외부 계약을 유지한다. | DONE |
| Problem | 강의·수강·결제 용어가 새 도메인 설명을 방해했다. | 외부 서비스명과 UI를 Asset·Request·Budget으로 바꾼다. | 코드·구성 DONE, 실행 READY |
| Problem | DB가 필요한 Context Test가 개발 환경에 강하게 의존했다. | 순수 도메인 테스트와 Docker 통합 테스트를 분리한다. | DONE |
| Try | 구매 전에 보유 대체재 확인을 넣는다. | X-01~03을 Sprint 2에 선택한다. | DONE |

### Sprint 2 Keep / Problem / Try

| 구분 | 내용 | 다음 액션 | 담당·기한 |
|---|---|---|---|
| Keep | Payment·Kafka 골격을 Budget 승인으로 재해석해 변경량을 줄였다. | 최소 수정 원칙을 최종 설명에 포함 | `TEAM INPUT` |
| Keep | 승인·반려 두 경로를 모두 검증했다. | 데모 데이터로 같은 경로 재확인 | `TEAM INPUT` |
| Problem | 소스 이름과 현재 컨테이너 이름이 다르다. | 허용 시 업무 서비스와 Gateway만 재빌드·재기동 | `TEAM INPUT` |
| Problem | 캡처와 실제 Review 피드백이 아직 없다. | 데모 리허설에서 캡처 후 참석자 피드백 기록 | `TEAM INPUT` |
| Problem | 공유 DB·직접 FK와 단일 테넌트는 운영 SaaS 경계로 부족하다. | 제품화 전 테넌트·DB 격리 Spike 수행 | `TEAM INPUT` |
| Try | 미구현 기술을 기능처럼 넣지 않고 Spike로 먼저 검증한다. | 아래 Spike 후보 중 다음 Sprint 1개만 선택 | PO 결정 |

실제 회고가 끝나면 각 Try에 반드시 담당자와 기한을 넣고, 다음 Sprint Planning 첫 5분에 상태를 확인한다.

## 10. 기술 Spike 후보

| ID | 확인할 불확실성 | Timebox | 산출물 | 구현 약속 여부 |
|---|---|---:|---|---|
| SPIKE-01 | 새 Eureka 이름과 제공 Gateway 이미지의 Route 호환 | 1시간 | 5개 서비스 UP 화면, Gateway 호출 결과 | 데모 전 필요 |
| SPIKE-02 | 동시 대여 승인 시 재고 음수·중복 차감 가능성 | 2시간 | 동시성 테스트와 잠금 전략 비교 | Future |
| SPIKE-03 | B2B SaaS 멀티테넌시의 DB 분리 방식 | 1일 | shared-schema / schema-per-tenant 비교 ADR | Future |
| SPIKE-04 | 카테고리를 넘는 의미 기반 대체재 추천 품질 | 1일 | 소량 평가셋, 규칙 기준선 대비 정확도 | Future |
| SPIKE-05 | Kafka 실패 시 DLT·Outbox 적용 범위 | 0.5일 | 장애 시나리오와 최소 구현안 | Future |
| SPIKE-06 | Request 장애 중 Asset 조회가 독립적으로 유지되는지 | 30분 | 서비스 중지 전·중·후 API 결과 | 실행 환경 변경 허용 후 |

Spike는 조사 결과를 얻기 위한 Timebox이며, 그 안에 운영 기능 완성을 약속하지 않는다.

## 11. 종료 체크

- [ ] 실제 팀원 이름과 Day 1·2 역할 순환을 기록했다.
- [ ] Daily Scrum 행을 팀 발언으로 최소 한 번 채웠다.
- [ ] Review 참석자 피드백과 PO 결정을 남겼다.
- [ ] 보드의 N-01·D-01 상태를 실제 결과로 갱신했다.
- [ ] Done SP를 최종 보드와 다시 합산했다.
- [ ] Retro Try마다 담당자와 기한을 정했다.
- [ ] 미완료 항목을 Product Backlog로 되돌렸다.
