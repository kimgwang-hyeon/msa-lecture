# 2일·2스프린트 운영 계획

## 1. 운영 원칙

- Day 1을 Sprint 1, Day 2를 Sprint 2로 운영한다.
- 각 Sprint는 `Planning → 실행 → 중간 Stand-up → 통합 → Review → Retro`로 닫는다.
- 문서보다 동작하는 흐름을 먼저 만들고, 문서는 결정과 검증 근거만 남긴다.
- 이 문서의 Review·Retro 내용은 현재 코드에서 확인된 관찰을 정리한 초안이다. 실제 팀 회의 후 `TEAM INPUT` 부분을 팀 의견으로 교체한다.

## 2. 팀 역할 배분안

이름을 알 수 없으므로 전공을 활용한 역할 후보만 제시한다. 한 사람이 한 역할에 고정될 필요는 없다.

| 배경 | 1차 역할 | Sprint 1 | Sprint 2 |
|---|---|---|---|
| 컴퓨터공학 A | Asset Backend | 교보재·재고 모델/API | 구매요청 상품 타입과 내부 API |
| 컴퓨터공학 B | Request Backend | 대여 신청·승인 상태 | 구매 신청과 Budget 연동 |
| 컴퓨터공학 C | Integration/Frontend | Gateway 연동·대여 UI | Kafka·승인 UI·통합 버그 수정 |
| 통계학 | Recommendation/QA | 테스트 데이터와 성공 지표 | 대체재 규칙·추천 평가 기준 |
| 전기전자공학 | Domain/QA | IoT·전자 교보재 시나리오, 재고 경계값 | 구매요청 입력값·장비 대체 가능성 검토 |
| 건축학 | UX/Domain | 공간·메이커 장비 사용자 여정 | 신청 폼·운영진 검토 정보 구조, 데모 스토리 |
| 기타 팀원 | PO·Scrum Master·문서·테스트 중 분담 | 백로그와 Review 진행 | 통합 검증·회고·발표 자료 준비 |

Scrum 역할은 `Product Owner 1명`, `Scrum Master 1명`, `Development Team 나머지 인원`으로 둔다. 전공은 역할 배분의 참고일 뿐이며, Day 2에는 PO·SM과 통합 담당을 순환해 모든 팀원이 제품·프로세스 관점을 경험하도록 한다.

### 팀 공통 약속

- API 요청·응답 변경은 [06_API_CONTRACT.md](./06_API_CONTRACT.md)에 먼저 반영한다.
- 담당 서비스 밖의 파일을 수정할 때 담당자에게 알린다.
- `main` 또는 공유 브랜치에 합치기 전 빌드와 핵심 테스트를 실행한다.
- 막힘은 기술 전체가 아니라 실패한 URL·요청·응답 단위로 공유한다.

## 3. Sprint 0 / 착수 전 60분

추가 Agile·MSA PDF의 Sprint 0 체크리스트를 이틀 실습 규모로 압축한다. 코딩을 시작하기 전에 아래 항목만 합의하고, 상세 문서는 실행 중 갱신한다.

| 시간 | 합의 내용 | 결과물 |
|---:|---|---|
| 10분 | B2B 고객과 문제 정의 | “교육기관·기업의 내부 장비 운영 SaaS” 한 문장, 첫 고객 SKALA |
| 10분 | PO·SM·개발팀과 Day 2 순환 역할 지정 | 팀원 이름이 들어간 역할표 (`TEAM INPUT`) |
| 15분 | Product Backlog 상위 항목 정제·MoSCoW | 상위 10개 Story와 인수 조건 |
| 10분 | 공통 DoD·API 우선 규칙 합의 | [03_PRODUCT_BACKLOG.md](./03_PRODUCT_BACKLOG.md)의 DoD·DoR |
| 10분 | Release·Sprint Goal과 용량 확정 | Day 1 대여, Day 2 구매·예산 Increment |
| 5분 | Sprint Board 열과 Daily 시각 결정 | `To Do / In Progress / Done`, 중간 15분 Stand-up |

Sprint 0는 별도 개발 스프린트가 아니다. 필요한 합의가 이미 문서에 있으면 확인만 하고 바로 Sprint 1로 들어간다.

## 4. 공통 하루 일정

| 시간대 | 이벤트 | 결과물 |
|---|---|---|
| 09:00~09:25 | Sprint Planning | Sprint Goal, 선택 백로그, 담당 |
| 09:25~12:00 | 병렬 구현 1 | 서비스별 첫 동작 |
| 12:00~13:20 | 점심 | - |
| 13:20~13:35 | Stand-up | 완료·다음 작업·막힘 공유 |
| 13:35~16:20 | 병렬 구현 2 | 프론트 연동과 오류 처리 |
| 16:20~17:10 | 통합 테스트·버그픽스 | 인수 조건 결과 |
| 17:10~17:35 | Sprint Review | 처음부터 끝까지 데모, 피드백 |
| 17:35~18:00 | Retrospective | Keep·Problem·Try와 다음 결정 |

## 5. Sprint 1 / Day 1

### Sprint Goal

교육생이 로그인 후 보유 교보재와 재고를 확인하고 대여를 신청하면, 운영진이 승인하여 신청 활성화와 재고 차감까지 완료되는 워킹 스켈레톤을 만든다.

### 선택한 범위

- 활용 서비스: Member, Asset, Request
- 인프라: Auth, Gateway, Eureka는 그대로 사용
- 제외: Budget 자동화, Kafka 상태 연동, Alternative 추천
- 프론트: 목록, 상세, 대여 신청, 운영진 대여 승인

### 워킹 스켈레톤

```text
로그인
 → GET /api/courses
 → GET /api/courses/{id}
 → POST /api/enrollments
 → GET /api/enrollments/pending?requestType=LOAN
 → POST /api/enrollments/{id}/approve
 → Asset 가용 재고 감소 + Request ACTIVE
```

### Sprint Backlog

`G-01`, `M-01`, `M-02`, `A-01`, `A-02`, `A-03`, `R-01`, `R-02`, `R-03`, `R-04`

### 작업 분해

| 트랙 | 작업 | 완료 증거 |
|---|---|---|
| Domain | Course에 자산 타입·수량을 최소 컬럼으로 추가 | Asset 응답에 `itemType`, `totalQuantity`, `availableQuantity` |
| Request | 대여 사유, PENDING, 승인·반려 전이 추가 | Request 응답의 `requestType=LOAN` |
| Integration | 승인 시 Asset 내부 API로 재고 차감 | 가용 수량 2 → 1 |
| Frontend | 교보재 목록·상세·대여 신청·승인 화면 | 요청 전·후 화면 |
| QA | 재고 0, 중복 신청, 중복 승인 확인 | 409 또는 검증 오류와 재고 비음수 |
| Documentation | 도메인 매핑·API 계약·Review 기록 | 본 문서 세트 |

### Review 시나리오

1. 교육생으로 로그인한다.
2. “조직이 보유 장비를 구성원에게 제공하는 B2B SaaS”라는 고객 가치를 한 문장으로 설명한다.
3. `iPhone 15 Pro`의 가용 수량이 2개인지 확인한다.
4. iOS 실기기 테스트 목적을 입력해 대여를 신청한다.
5. 운영진 화면에서 `PENDING` 요청을 확인한다.
6. 승인 후 신청이 `ACTIVE`, 가용 수량이 1개인지 확인한다.

### 현재 코드 기준 Review 결과

- 대여 신청 생성: 통과
- 운영진 대기 목록 조회: 통과
- 승인 후 Request `ACTIVE`: 통과
- Asset 가용 수량 `2 → 1`: 통과
- Kafka 없이도 핵심 대여 가치 제공: 통과

### Sprint 1 Retro 초안

| 구분 | 관찰 |
|---|---|
| Keep | 기존 Course·Enrollment API와 테이블을 재사용해 짧은 시간에 워킹 스켈레톤을 만들었다. |
| Problem | 코드 용어가 강의·수강·결제로 남아 팀 도메인을 설명하기 어려웠다. |
| Problem | 로컬 전체 Context Test는 Docker DB 주소 때문에 바로 실행되지 않았다. |
| Try | Sprint 2에서 서비스 논리 이름을 바꾸고, 순수 단위 테스트와 Docker 통합 테스트를 분리한다. |
| Try | 구매요청 전에 대체재를 확인하게 해 중복 구매 문제를 직접 다룬다. |

`TEAM INPUT`: 실제 Review에서 나온 사용자·교수 피드백과 팀원별 한 줄 회고를 추가한다.

## 6. Sprint 2 / Day 2

### Sprint Goal

보유하지 않은 교보재를 구매 요청하면 대체재 확인과 예산 검토를 거치고, 운영진의 승인·반려 결과가 Kafka를 통해 신청 상태에 자동 반영되는 전체 흐름을 완성한다.

### Sprint 1 Review를 반영한 결정

- 실제 결제를 구현하지 않고 Budget Service로 의미를 바꾼다.
- 구매 중복을 줄이는 핵심 가치에 맞춰 Alternative Service를 구매 폼 앞에 배치한다.
- 기존 `payment.completed`, `enrollment.completed` 토픽 구조는 유지한다.
- 업무 서비스명은 GearHub 이름으로 바꾸되 포트·API·테이블은 유지한다.

### 선택한 범위

- Alternative: 동일 카테고리·가용 재고 기반 최대 5개 추천
- Request: 구매요청 입력·검증·상태
- Asset: `PURCHASE_REQUEST` 상품과 링크
- Budget: 총액·PENDING·승인·반려
- Kafka: Budget 결과를 Request 상태에 반영
- Frontend: 구매요청 단계, 내 신청, 운영진 예산 검토

### 확장 흐름

```text
구매요청 폼
 → Alternative 대체재 조회
 → POST /api/enrollments/purchases
 → Asset에 비공개 PURCHASE_REQUEST 생성
 → Budget PENDING 생성
 → 운영진 Budget 승인/반려
 → payment.completed(status=COMPLETED|FAILED)
 → Request ACTIVE|REJECTED
 → enrollment.completed
```

### Sprint Backlog

`X-01`, `X-02`, `X-03`, `B-01`, `B-02`, `E-01`, `F-01`, `N-01`, `N-02`, `D-01`

### 작업 분해

| 트랙 | 작업 | 완료 증거 |
|---|---|---|
| Recommendation | 동일 카테고리의 `OWNED && availableQuantity > 0` 조회 | `/api/recommend/alternatives` 응답 |
| Request | URL·단가·수량·사유·대체재 확인 검증 | 잘못된 요청 400, 정상 요청 PENDING |
| Budget | 서버 계산 총액과 승인·반려 API | `PENDING → COMPLETED/FAILED` |
| Event | 결과 status를 Kafka payload에 포함 | Request `ACTIVE/REJECTED` 자동 전환 |
| Frontend | 단계형 구매요청과 운영진 승인 화면 | 대체재 확인 후에만 제출 가능 |
| Naming | Eureka·Compose·Gateway 목적지를 새 서비스명으로 정리 | Compose config 검증 |
| QA | 승인·반려 E2E, 로그·빌드 검사 | [07_VALIDATION_AND_DEMO.md](./07_VALIDATION_AND_DEMO.md) |

### Review 시나리오

1. 교육생이 `ELECTRONICS_IOT` 카테고리 구매요청을 시작한다.
2. Raspberry Pi·Arduino 등 대여 가능한 보유 대체재를 확인한다.
3. Jetson 키트의 링크, 단가, 수량과 사유를 제출한다.
4. 운영진 화면에서 Budget `PENDING`과 총액을 확인한다.
5. 예산 승인 후 Budget `COMPLETED`, Request `ACTIVE`를 확인한다.
6. 별도 요청을 반려해 Budget `FAILED`, Request `REJECTED`도 확인한다.

### 현재 코드 기준 Review 결과

- 구매요청과 Budget PENDING 생성: 통과
- 예산 승인 이벤트와 Request ACTIVE: 통과
- 예산 반려 이벤트와 Request REJECTED: 통과
- Alternative 내부 조회 규칙: 통과
- 프론트 Production Build: 통과
- 새 논리 서비스명 구성: 소스·Compose 검증 완료, 컨테이너 재기동 전(`READY`)
- 인증 사용자의 전체 화면 캡처: `TEAM INPUT`

### Sprint 2 Retro 초안

| 구분 | 관찰 |
|---|---|
| Keep | Payment와 Kafka 구조를 버리지 않고 예산 승인이라는 도메인으로 치환했다. |
| Keep | 승인·반려 두 경로를 모두 통합 테스트해 상태 이벤트를 검증했다. |
| Problem | 서비스명을 바꾸면 제공 Gateway 이미지의 고정 라우팅과 충돌할 수 있었다. |
| Problem | 현재 Alternative는 카테고리 규칙이라 의미가 비슷하지만 카테고리가 다른 장비는 찾지 못한다. |
| Try | Gateway 이미지는 유지하고 Compose 환경변수로 새 Eureka 목적지를 연결한다. |
| Try | 다음 스프린트 후보로 의미 기반 유사도 추천과 추천 품질 지표를 둔다. |

`TEAM INPUT`: 실제 팀원별 `잘한 점 1개 / 아쉬운 점 1개 / 다음에 바꿀 점 1개`를 추가한다.

## 7. 위험과 대응

| 위험 | 영향 | 대응 | 담당 후보 |
|---|---|---|---|
| 외부 Docker Registry 지연 | 전체 재빌드 지연 | 사전 이미지 확인, 서비스별 빌드, 네트워크 정상 시 최종 클린 빌드 | Integration |
| 기존 DB 볼륨의 컬럼 차이 | 실행 시 스키마 오류 | `ddl-auto:update` 확인, 초기 DDL과 실제 컬럼 비교 | Backend |
| Kafka 이벤트 중복 | 재처리 시 잘못된 상태 전이 | 이미 종결된 상태는 멱등 처리 | Request/Budget |
| 운영진 권한이 UI에만 의존 | 직접 API 호출 위험 | MVP 이후 서버 측 역할 검증 백로그 `S-01` | Security |
| 팀원 병렬 수정 충돌 | 통합 지연 | 서비스·문서 담당 분리, API 계약 우선 | Scrum Master |
| 화면 캡처 시 데이터 상태 불일치 | 데모 흐름 단절 | 데모 계정·교보재·요청 순서를 사전 고정 | QA |

## 8. 실제 팀 회의 입력란

- Product Owner: `TEAM INPUT`
- Scrum Master: `TEAM INPUT`
- Sprint 1 Review 참석자·피드백: `TEAM INPUT`
- Sprint 2 Review 참석자·피드백: `TEAM INPUT`
- 최종 회고 액션 아이템과 담당·기한: `TEAM INPUT`

보드 이동, Daily Scrum, 속도와 계획 대비 실적은 [08_AGILE_EXECUTION_LOG.md](./08_AGILE_EXECUTION_LOG.md)에 이어서 기록한다.
