# Product Backlog

## 1. 우선순위 원칙

1. 사용자가 처음부터 끝까지 수행할 수 있는 한 흐름을 먼저 만든다.
2. 핵심 가치가 없는 기술 작업은 뒤로 미룬다.
3. 기존 API·테이블·토픽을 재사용할 수 있으면 새 구조를 만들지 않는다.
4. 상태 일관성과 재고 안전성처럼 데모 실패로 이어지는 항목을 화면 장식보다 우선한다.
5. `Must`가 완료되기 전에는 `Could`를 시작하지 않는다.

## 2. 백로그

| ID | 사용자 스토리 또는 작업 | 서비스 | 우선순위 | SP | Sprint | 상태 |
|---|---|---|---:|---:|---|---|
| G-01 | 템플릿 도메인을 GearHub 이해관계자와 용어로 매핑한다. | 전체 | Must | 2 | S1 | DONE |
| M-01 | 교육생·운영진이 기존 OAuth2 로그인과 역할을 그대로 사용한다. | Member/Infra | Must | 1 | S1 | DONE |
| M-02 | 로그인한 구성원이 자신의 이름과 교육생·운영진 역할을 확인한다. | Member/Frontend | Must | 1 | S1 | DONE |
| A-01 | 교육생이 보유 교보재 목록과 가용 수량을 조회한다. | Asset/Frontend | Must | 3 | S1 | DONE |
| A-02 | 교육생이 교보재 상세, 자산가치와 전체·가용 수량을 확인한다. | Asset/Frontend | Must | 2 | S1 | DONE |
| A-03 | 운영진이 보유 교보재와 수량을 등록한다. | Asset/Frontend | Should | 3 | S1 | DONE |
| R-01 | 교육생이 사용 목적을 입력해 대여를 신청한다. | Request | Must | 3 | S1 | DONE |
| R-02 | 운영진이 대여 승인 대기 목록을 조회한다. | Request/Frontend | Must | 2 | S1 | DONE |
| R-03 | 운영진 승인 시 신청을 활성화하고 가용 재고를 1 차감한다. | Request/Asset | Must | 5 | S1 | DONE |
| R-04 | 운영진이 사유를 입력해 대여를 반려한다. | Request/Frontend | Should | 2 | S1 | DONE |
| X-01 | 교육생이 신규 구매 전에 같은 카테고리의 보유 대체재를 확인한다. | Alternative/Asset | Must | 3 | S2 | DONE |
| X-02 | 교육생이 상품명·단가·수량·URL·사유를 입력해 구매를 요청한다. | Request/Asset | Must | 5 | S2 | DONE |
| X-03 | 교육생이 대체재가 추천된 기준과 신규 요청을 계속할 조건을 확인한다. | Alternative/Frontend | Should | 1 | S2 | DONE |
| B-01 | 시스템이 서버에서 총액을 계산해 예산 요청을 `PENDING`으로 만든다. | Request/Budget | Must | 3 | S2 | DONE |
| B-02 | 운영진이 구매 링크와 총액을 보고 예산을 승인·반려한다. | Budget/Frontend | Must | 3 | S2 | DONE |
| E-01 | Budget 결과를 기존 Kafka 토픽으로 전달해 Request 상태를 자동 변경한다. | Budget/Request/Kafka | Must | 5 | S2 | DONE |
| F-01 | 교육생이 대여·구매 신청 상태와 반려 사유를 확인한다. | Frontend/Request | Must | 3 | S2 | DONE |
| N-01 | 업무 서비스명을 GearHub 도메인명으로 변경하고 Gateway 호환 설정을 둔다. | 전체 | Should | 2 | S2 | READY |
| N-02 | 단위·통합·프론트 빌드 검증 결과를 기록한다. | 전체 | Must | 3 | S2 | DONE |
| D-01 | 요청 전·후, 예산 승인, Eureka 화면을 캡처한다. | 문서/QA | Must | 2 | S2 | TEAM INPUT |
| S-01 | 운영진 전용 API에 서버 측 역할 권한을 강제한다. | Request/Budget/Asset | Should | 3 | 이후 | FUTURE |
| O-01 | 반납 시 재고를 복구하고 대여를 종료한다. | Request/Asset | Could | 5 | 이후 | FUTURE |
| O-02 | 대여 시작·종료일과 예약 충돌을 관리한다. | Request | Could | 8 | 이후 | FUTURE |
| AI-01 | 문장 임베딩으로 카테고리를 넘는 유사 대체재를 추천한다. | Alternative | Could | 8 | 이후 | FUTURE |
| O-03 | 승인·반려 알림과 실제 발주 상태를 연동한다. | 신규 범위 | Won't | 13 | MVP 제외 | FUTURE |
| SAAS-01 | 고객 조직별 테넌트를 만들고 업무 데이터를 격리한다. | SaaS Platform | Must(제품화) | 13 | 이후 | FUTURE |
| SAAS-02 | 고객사 SSO와 조직·부서·역할 권한을 연결한다. | Member/Infra | Must(제품화) | 8 | 이후 | FUTURE |
| SAAS-03 | 조직별 승인 단계와 한도액을 설정한다. | Request/Budget | Should(제품화) | 8 | 이후 | FUTURE |
| SAAS-04 | 자산 활용률·승인 리드타임·구매 절감 추정치를 제공한다. | Analytics | Should(제품화) | 8 | 이후 | FUTURE |
| SAAS-05 | 자산·요청·승인 변경 이력을 감사로그로 남긴다. | Audit | Should(제품화) | 5 | 이후 | FUTURE |
| SAAS-06 | 고객 계약과 SaaS 요금제를 관리한다. | SaaS Platform | Could(제품화) | 13 | 이후 | FUTURE |

`SP`는 상대 난이도이며 실제 시간 약속이 아니다. 제공 템플릿이 있으므로 신규 구축보다 작은 값을 사용했다.

## 3. Sprint별 범위 합계

| Sprint | 핵심 목표 | 선택 항목 | 합계 SP |
|---|---|---|---:|
| Sprint 1 / Day 1 | 대여 워킹 스켈레톤 | G-01, M-01~02, A-01~03, R-01~04 | 24 |
| Sprint 2 / Day 2 | 구매·예산·이벤트·추천 확장 | X-01~03, B-01~02, E-01, F-01, N-01~02, D-01 | 30 |

인원이 여러 명인 팀에서 API 계약을 먼저 고정하고 백엔드·프론트·검증을 병렬 진행한다는 전제다. Sprint 2가 과부하이면 `N-01`과 캡처 정리는 기능 완료 뒤 처리한다.

## 4. Epic → User Story → Task 계층

| Epic | 제품 가치 | 포함 User Story | 대표 Task |
|---|---|---|---|
| EPIC-1 조직 구성원 접근 | 조직 구성원이 역할에 맞는 화면을 사용 | M-01~02 | OAuth2 유지, 사용자 정보 표시, Route Guard 확인 |
| EPIC-2 보유 자산 가시성 | 문의 없이 사용 가능한 내부 장비를 발견 | A-01~03 | 자산 컬럼·API·목록·상세·등록 UI |
| EPIC-3 장비 대여 | 신청부터 승인과 재고 차감까지 추적 | R-01~04 | 신청 DTO, 상태 전이, Asset REST 호출, 승인 UI |
| EPIC-4 신규 구매요청 | 구매 근거를 표준화하고 중복 구매를 줄임 | X-01~03, B-01~02, F-01 | 대체재 조회, 구매 폼, 총액 계산, 검토 화면 |
| EPIC-5 상태 자동화 | 서비스 간 승인 결과를 느슨하게 전파 | E-01 | Kafka payload, Consumer 상태 전이, 승인·반려 테스트 |
| EPIC-6 실습 증거 | Increment가 재현 가능함을 증명 | N-01~02, D-01 | Compose 검증, 테스트 기록, 화면 캡처 |
| EPIC-7 B2B SaaS 제품화 | 여러 조직에 안전하게 제공 | SAAS-01~06 | 테넌트·SSO·정책·분석·감사·요금제 설계 |

## 5. 상위 10개 Story 정제와 INVEST 점검

상위 기능 Story는 사용자가 얻는 결과 단위로 작게 나눴다. `R-03`, `X-02`, `E-01`은 서비스 간 의존성이 있으므로 완전히 독립적이지 않지만, 필요한 API 계약과 선행 Story를 명시해 Sprint 안에서 검증 가능하게 만들었다.

| Story | 독립성·협상 가능성 | 사용자 가치 | 추정 가능·작은 범위 | 테스트 근거 |
|---|---|---|---|---|
| M-02 | 기존 로그인 계약만 사용 | 자신의 권한을 즉시 인지 | 1 SP | 헤더의 이름·역할 확인 |
| A-01 | 읽기 기능만 단독 제공 | 보유·가용 장비 발견 | 3 SP | AC-01 목록 응답 |
| A-02 | A-01 이후 상세로 분리 | 신청 전 판단 정보 제공 | 2 SP | 수량·자산가치 표시 |
| R-01 | 승인과 분리해 신청만 생성 | 사용 목적이 있는 요청 기록 | 3 SP | AC-02 PENDING |
| R-03 | R-01·Asset 계약 필요 | 승인과 재고가 일치 | 5 SP | AC-03·04 |
| R-04 | 승인 경로와 별도 선택 가능 | 반려 이유 전달 | 2 SP | REJECTED·사유 확인 |
| X-01 | 구매 생성 전 독립 조회 | 중복 구매 예방 | 3 SP | AC-05 |
| X-02 | X-01 확인 결과만 입력 계약으로 사용 | 표준화된 구매 근거 | 5 SP | AC-06 |
| B-02 | PENDING Budget을 전제로 독립 검토 | 조직 예산 결정 기록 | 3 SP | AC-07·08 |
| E-01 | Budget·Request 이벤트 계약 필요 | 상태 수동 동기화 제거 | 5 SP | Kafka 후 상태 확인 |

SP는 Planning Poker에서 사용할 Fibonacci 계열 `1, 2, 3, 5, 8, 13`으로 제한했다. 실제 팀 추정이 달라지면 Planning에서 합의한 값으로 교체한다.

## 6. 핵심 인수 조건

### AC-01 보유 교보재 조회

```gherkin
Given 운영진이 수량 2개의 보유 교보재를 등록했고
When 교육생이 교보재 목록과 상세를 조회하면
Then itemType은 OWNED이고 totalQuantity와 availableQuantity가 각각 2로 보인다.
```

### AC-02 대여 신청

```gherkin
Given 대여 가능한 교보재와 로그인한 교육생이 있고
When 교육생이 사용 목적과 함께 대여를 신청하면
Then LOAN 요청이 PENDING으로 생성되고 재고는 아직 감소하지 않는다.
```

### AC-03 대여 승인과 재고 차감

```gherkin
Given PENDING 상태의 LOAN 요청과 가용 재고 2개가 있고
When 운영진이 요청을 승인하면
Then 요청은 ACTIVE가 되고 가용 재고는 1개가 된다.
And 같은 요청을 다시 승인해 재고를 중복 차감할 수 없다.
```

### AC-04 재고 부족

```gherkin
Given 가용 수량이 0인 교보재가 있고
When 대여를 신청하거나 승인하려 하면
Then 시스템은 재고 부족을 알리고 수량을 음수로 만들지 않는다.
```

### AC-05 대체재 선확인

```gherkin
Given 교육생이 신규 구매요청 카테고리를 선택했고
When 구매요청을 제출하려 하면
Then 같은 카테고리의 대여 가능한 보유 교보재를 먼저 조회해야 한다.
And alternativeChecked가 true가 아니면 서버가 요청을 거절한다.
```

### AC-06 구매요청과 금액

```gherkin
Given 교육생이 단가 499000원, 수량 2개와 유효한 URL을 입력했고
When 구매요청을 제출하면
Then PURCHASE Request와 비공개 PURCHASE_REQUEST Asset이 생성된다.
And Budget amount는 서버에서 계산한 998000원이고 상태는 PENDING이다.
```

### AC-07 예산 승인

```gherkin
Given PENDING Budget과 연결된 PURCHASE Request가 있고
When 운영진이 예산을 승인하면
Then Budget은 COMPLETED와 승인번호를 가진다.
And payment.completed 이벤트의 status는 COMPLETED이다.
And Request는 Kafka 소비 후 ACTIVE가 된다.
```

### AC-08 예산 반려

```gherkin
Given PENDING Budget과 연결된 PURCHASE Request가 있고
When 운영진이 예산을 반려하면
Then Budget은 FAILED가 된다.
And Request는 Kafka 소비 후 REJECTED와 반려 설명을 가진다.
```

## 7. Definition of Ready

스토리를 Sprint에 넣기 전에 다음이 준비되어야 한다.

- [ ] 사용자와 가치가 한 문장으로 설명된다.
- [ ] 호출할 API와 담당 서비스가 정해졌다.
- [ ] 입력·출력 필드와 오류 조건을 알고 있다.
- [ ] 프론트와 백엔드가 같은 완료 조건에 합의했다.
- [ ] Auth/Gateway/Eureka 내부 수정이 필요한 항목이 아니다.
- [ ] 이틀 MVP 범위를 벗어나면 백로그로 되돌린다.

## 8. Definition of Done

- [ ] 코드가 빌드된다.
- [ ] 핵심 도메인 단위 테스트가 통과한다.
- [ ] Gateway 기준 요청·응답 계약이 문서와 일치한다.
- [ ] 정상 흐름과 대표 오류 흐름을 각각 확인한다.
- [ ] 상태 변경이 DB와 화면에서 일치한다.
- [ ] 다른 서비스의 기존 API·포트·토픽을 깨뜨리지 않는다.
- [ ] Review에서 처음부터 끝까지 데모할 수 있다.
- [ ] 알려진 제한과 다음 스프린트 항목을 기록한다.

## 9. 변경 요청 처리 규칙

- Sprint 중 새 아이디어가 나오면 먼저 Product Backlog에 적는다.
- Sprint Goal에 직접 필요한 버그만 현재 Sprint에 추가한다.
- 새로운 서비스·테이블·토픽이 필요한 변경은 팀 합의 없이 시작하지 않는다.
- 데모를 막지 않는 디자인 개선은 핵심 흐름 완료 후 진행한다.
