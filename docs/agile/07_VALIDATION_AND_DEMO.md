# 검증 결과와 데모 체크리스트

## 1. 검증 범위

검증일: 2026-08-26

현재 소스의 도메인 로직, 서비스 간 REST, Kafka 상태 전파, 프론트 빌드와 Compose 구성을 확인했다. 핵심 API 통합 검증은 각 서비스 포트에서 수행했으며, 새 서비스명으로 Docker를 재기동한 뒤 Gateway 인증 흐름과 화면 캡처를 최종 확인해야 한다.

## 2. 자동·구성 검사

| 검사 | 결과 | 비고 |
|---|---|---|
| Member Service `gradlew classes` | PASS | 새 `member-service` 애플리케이션명 포함 |
| Asset 도메인 단위 테스트 | PASS | 수량 기본값·재고 차감·오류 조건 |
| Request 도메인 단위 테스트 | PASS | 승인·반려 상태 전이 |
| Budget 도메인 단위 테스트 | PASS | PENDING에서만 승인·반려 |
| Alternative Python `compileall` | PASS | 스키마·라우터 문법 확인 |
| Vue `vite build` | PASS | 111 modules transformed |
| `docker compose config --quiet` | PASS | 새 5개 업무 서비스명과 의존성 유효 |
| Gateway Route 환경변수 확인 | PASS | `lb://member/asset/request/budget/alternative-service` |

## 3. 통합 인수 테스트 결과

| TC | 시나리오 | 기대 결과 | 실제 결과 | 상태 |
|---|---|---|---|---|
| TC-01 | 보유 교보재 목록 조회 | `OWNED`, `ACTIVE` 항목과 가용 수량 반환 | 시연용 보유 교보재 8개 반환 | PASS |
| TC-02 | iPhone 대여 신청 | LOAN Request `PENDING`, 재고 유지 | Request ID 1 PENDING, 재고 2 | PASS |
| TC-03 | 대여 승인 | Request `ACTIVE`, 재고 1 차감 | ACTIVE, 재고 `2 → 1` | PASS |
| TC-04 | 신규 Jetson 구매요청 | PURCHASE Request와 Budget `PENDING` | Request ID 2, Budget ID 1 PENDING | PASS |
| TC-05 | 예산 승인 | Budget `COMPLETED`, Kafka 후 Request `ACTIVE` | 승인번호 생성, Request ACTIVE | PASS |
| TC-06 | 열화상 카메라 예산 반려 | Budget `FAILED`, Kafka 후 Request `REJECTED` | FAILED, 반려 설명과 REJECTED | PASS |
| TC-07 | Kafka 소비·후속 발행 | Budget 이벤트 소비 후 Request 상태 변경, 승인 시 후속 이벤트 | 두 토픽의 Producer/Consumer 성공 로그 확인 | PASS |
| TC-08 | 동일 카테고리 대체재 데이터 | 보유·가용 항목만 최대 5개 | Asset 내부 추천에서 Raspberry Pi·Arduino 등 반환 | PASS |
| TC-09 | Alternative 외부 인증 호출 | 로그인 토큰으로 Gateway 경유 성공 | 무토큰 직접 호출 403 확인, 인증 화면 E2E는 재기동 후 필요 | READY |
| TC-10 | 새 Eureka 이름 등록 | 5개 새 업무 서비스명이 UP | 소스·Compose만 변경, 이미지·컨테이너 미변경 | READY |
| TC-11 | 프론트가 Mock이 아닌 API 모듈 사용 | 목록·신청·승인 화면이 실제 서비스 응답 사용 | 모든 주요 View가 Axios API 모듈을 호출하고 서비스 통합 데이터로 검증 | PASS |
| TC-12 | Request 중지 시 Asset 조회 독립성 | Request 장애 중에도 Asset 조회 정상 | 현재 컨테이너 상태 보존 요청으로 중지 실험 미수행 | READY |
| TC-13 | Swagger `Try it out` | 팀원별 담당 API 최소 1회 실제 호출 | 4개 Spring 서비스의 Swagger 설정 확인, 팀별 실행 증거 미수집 | TEAM INPUT |

## 4. 사용한 시연 데이터

### 보유 교보재

- iPhone 15 Pro
- Galaxy S24 Ultra
- MacBook Pro 14 M3
- Raspberry Pi 5 IoT Kit
- Arduino Uno Sensor Kit
- AWS 실습 크레딧
- Insta360 X4
- 레이저 거리 측정기

### 구매요청 사례

- 승인 사례: NVIDIA Jetson Orin Nano 개발자 키트, 499,000원 × 1
- 반려 사례: 휴대용 열화상 카메라, 420,000원 × 1

## 5. 새 서비스명 적용 후 최종 재검증

사용자의 요청으로 현재 Docker 이미지는 건드리지 않았다. 나중에 재빌드·재기동을 허용할 때 다음 순서로 확인한다.

1. 새 업무 서비스만 빌드·기동한다.
2. API Gateway도 환경변수 Route를 적용하도록 재생성한다.
3. Eureka에서 `MEMBER-SERVICE`, `ASSET-SERVICE`, `REQUEST-SERVICE`, `BUDGET-SERVICE`, `ALTERNATIVE-SERVICE`가 `UP`인지 확인한다.
4. 학생과 운영진 계정으로 Gateway `:8080`을 경유해 로그인·대여·구매 흐름을 반복한다.
5. 핵심 서비스 로그에 `ERROR`, `Traceback`, 처리되지 않은 예외가 없는지 확인한다.

## 6. 화면 캡처 목록

캡처는 [evidence/screenshots](../evidence/screenshots/)에 아래 파일명으로 저장한다.

| 파일명 | 화면 | 반드시 보여야 할 정보 | 상태 |
|---|---|---|---|
| `01_landing.png` | GearHub 시작 화면 | 서비스 목적과 핵심 가치 | TEAM INPUT |
| `02_eureka-services.png` | Eureka Dashboard | 새 5개 업무 서비스명과 UP 상태 | READY |
| `03_asset-list.png` | 교보재 목록 | 여러 카테고리와 가용 수량 | TEAM INPUT |
| `04_asset-detail-before.png` | 대여 전 상세 | iPhone 가용 수량 2, 신청 사유 입력 | TEAM INPUT |
| `05_loan-pending.png` | 운영진 승인 화면 | LOAN PENDING과 사용 목적 | TEAM INPUT |
| `06_asset-detail-after.png` | 대여 승인 후 | Request ACTIVE와 가용 수량 1 | TEAM INPUT |
| `07_purchase-alternatives.png` | 신규 구매요청 | 같은 카테고리 보유 대체재 | TEAM INPUT |
| `08_budget-pending.png` | 예산 검토 | 상품 링크·수량·총액·PENDING | TEAM INPUT |
| `09_purchase-approved.png` | 내 신청 | Budget 승인 후 PURCHASE ACTIVE | TEAM INPUT |
| `10_purchase-rejected.png` | 내 신청 | PURCHASE REJECTED와 반려 설명 | TEAM INPUT |
| `11_swagger-api.png` | Swagger UI | 담당 API의 실제 요청·응답 | TEAM INPUT |
| `12_sprint-board.png` | Sprint Board | To Do·In Progress·Done과 최종 Done SP | TEAM INPUT |

### 캡처 원칙

- 요청 전과 후가 비교되도록 같은 교보재·같은 계정을 사용한다.
- 브라우저 주소가 가능하면 `localhost:3000`, API는 Gateway `localhost:8080` 기준임을 보여준다.
- Access Token, 비밀번호, Client Secret은 화면에 노출하지 않는다.
- 테스트 데이터와 캡처 순서를 고정해 발표 직전 상태가 달라지지 않게 한다.

## 7. 5분 데모 시나리오

### 0:00~0:40 문제와 가치

“교육기관과 기업은 내부 장비의 재고·대여·구매요청이 여러 채널에 흩어져 있습니다. GearHub는 구성원의 보유 재고 확인부터 자산 운영자·예산 승인자의 검토까지 한 흐름으로 연결하는 B2B SaaS입니다. 이번 데모는 SKALA 한 조직을 위한 첫 번째 Increment입니다.”

### 0:40~2:00 Sprint 1 결과

1. 교육생으로 교보재 목록을 조회한다.
2. iPhone 상세의 가용 수량을 확인한다.
3. 사용 목적과 함께 대여를 신청한다.
4. 운영진이 신청을 승인한다.
5. 신청 상태와 재고 감소를 보여준다.

### 2:00~4:10 Sprint 2 결과

1. 신규 교보재 구매요청 화면에서 카테고리를 고른다.
2. 보유 대체재가 먼저 추천되는 것을 보여준다.
3. 상품 링크·단가·수량·사유를 제출한다.
4. 운영진이 예상 총액을 보고 예산을 승인한다.
5. Kafka를 통해 구매 신청이 ACTIVE가 되는 것을 보여준다.
6. 반려 사례의 상태와 설명도 짧게 보여준다.

### 4:10~5:00 Agile·MSA 학습점

- Sprint 1에서는 Member·Asset·Request만으로 사용자 가치를 만들었다.
- Sprint 2에서 Budget·Kafka·Alternative를 독립적으로 확장했다.
- Auth·Gateway·Eureka는 제공 인프라 계약을 유지했다.
- 현재 추천은 설명 가능한 규칙 기반 기준선이며 의미 기반 AI 추천은 다음 실험이다.

## 8. 10~12분 기술 데모 시나리오

추가 Agile·MSA PDF의 예시 데모 시간을 GearHub에 맞게 바꾼 버전이다. 발표 시간이 5분이면 앞 절을 사용한다.

| 시간 | 시연 | 확인할 증거 |
|---|---|---|
| 0:00~1:00 | B2B 고객·문제·가치 | 교육기관·기업, 중복 구매, 자산 활용률, 단일 테넌트 MVP 범위 |
| 1:00~2:00 | 교육생·운영진 로그인 | OAuth2, 이름·역할, 역할별 메뉴 |
| 2:00~3:00 | Eureka와 MSA 구성 | 제공 인프라와 5개 GearHub 업무 서비스의 `UP` 상태 |
| 3:00~6:00 | Sprint 1 대여 흐름 | 보유 수량 2 → 신청 PENDING → 승인 ACTIVE → 가용 수량 1 |
| 6:00~9:30 | Sprint 2 구매·예산 흐름 | 대체재 이유, 링크·수량·총액, Budget 승인·반려 |
| 9:30~10:30 | Kafka 상태 전파 | `payment.completed`와 Request `ACTIVE/REJECTED` |
| 10:30~11:30 | Agile 실행 증거 | Sprint Board, Done SP, 계획 대비 미완료 항목, 회고 액션 |
| 11:30~12:00 | 제한·다음 실험 | 멀티테넌시·권한·반납·의미 기반 추천은 Future라고 명시 |

## 9. 발표 전 Smoke Test

- [ ] Docker 컨테이너와 프론트가 모두 실행 중이다.
- [ ] Eureka에 새 5개 서비스가 `UP`이다.
- [ ] 교육생·운영진 계정 로그인이 된다.
- [ ] Gateway를 통한 교보재 목록 조회가 된다.
- [ ] 데모용 교보재의 재고가 계획한 값이다.
- [ ] 대기 중인 대여·예산 요청이 중복으로 남아 있지 않다.
- [ ] 승인·반려 버튼을 누른 뒤 화면이 갱신된다.
- [ ] Kafka 이벤트 후 Request 상태가 바뀐다.
- [ ] 구매 링크가 유효하고 새 탭에서 열린다.
- [ ] Swagger에서 담당 API를 실제 데이터로 호출했다.
- [ ] 팀 밖 Review 참석자에게 최소 1개 피드백을 받았다.
- [ ] 비밀정보가 캡처나 로그 화면에 노출되지 않는다.

## 10. 알려진 제한

- 운영진 API의 서버 측 역할 강제는 후속 보강 항목이다.
- 반납과 재고 복구가 없어 승인된 대여는 자동 종료되지 않는다.
- 구매요청 상품은 승인 후 실제 보유 Asset으로 자동 입고되지 않는다.
- Alternative는 카테고리 기반 규칙이며 AI 모델을 사용하지 않는다.
- 현재 실행 컨테이너는 새 서비스명으로 재기동하기 전이다.
- 현재 데이터 모델은 조직 구분이 없는 단일 테넌트라 완성형 B2B SaaS는 아니다.
