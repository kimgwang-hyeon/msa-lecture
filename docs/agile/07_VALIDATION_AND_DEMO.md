# GearHub Campus 검증과 데모

## 1. 검증 목표

검증은 화면이 열리는지만 확인하지 않는다. 다음 세 가지를 함께 증명한다.

1. 대여·반납·도입 상태와 재고가 업무 규칙대로 변한다.
2. MSA의 동기 호출과 Kafka 이벤트가 종단 간 연결된다.
3. AI가 기준선보다 나은지 측정되고 관리자 행동으로 이어진다.

상세 수치와 명령의 최종 근거는 [10_FINAL_VALIDATION.md](./10_FINAL_VALIDATION.md)다.

## 2. 핵심 인수 결과

| ID | 검증 항목 | 기대 결과 | 결과 |
|---|---|---|---|
| V-01 | 전체 기동 | Web 포함 11개 컨테이너 실행 | PASS |
| V-02 | 인증·그룹 | 관리자 로그인 후 8개 그룹 조회 | PASS |
| V-03 | 자산 범위 | 공용 + 현재 그룹 자산 조회 | PASS |
| V-04 | 대여 승인 | PENDING → ACTIVE, 재고 1 감소 | PASS |
| V-05 | 반납 요청 | ACTIVE → RETURN_REQUESTED, 재고 유지 | PASS |
| V-06 | 반납 확인 | RETURN_REQUESTED → RETURNED, 재고 1 복구 | PASS |
| V-07 | 도입 그룹 승인 | PENDING → GROUP_APPROVED, Budget 생성 | PASS |
| V-08 | 예산 승인 | payment.completed 후 BUDGET_APPROVED | PASS |
| V-09 | 입고 | RECEIVED, Asset OWNED / ACTIVE와 수량 반영 | PASS |
| V-10 | 분석 이벤트 | rental.lifecycle가 LIVE로 저장 | PASS |
| V-11 | 모델 평가 | 테스트 WAPE가 기준선보다 낮음 | PASS |
| V-12 | 관리자 화면 | 4주 예측, 부족, 이동 제안 표시 | PASS |
| V-13 | 재고 무결성 | 가용 수량 음수·총수량 초과 0건 | PASS |

## 3. 재현 가능한 데모 데이터

고정 시드 42의 기준:

| 데이터 | 수량 | 사용 화면 |
|---|---:|---|
| 활성 그룹 | 8 | 그룹 목록 |
| 대여 가능 자산 | 120 | 자산 카탈로그 |
| 운영 대여 요청 | 200 | 내 요청·관리자 승인 |
| 도입 요청 | 8 | 그룹·학교 검토 |
| SIMULATION 이벤트 | 10,814 | 모델 학습·평가 |
| 최신 4주 예측 행 | 224 | 그룹 분석 |
| LIVE 이벤트 | 1 이상 | Kafka 소비 확인 |

데모 계정:

| 역할 | 이메일 | 비밀번호 |
|---|---|---|
| 학교·그룹 관리자 | campus.admin@demo.local | GearHub123! |
| 일반 구성원 | campus.member@demo.local | GearHub123! |

시드 재실행:

~~~powershell
docker compose exec -T alternative-service python scripts/seed_demo_data.py
~~~

시드 스크립트는 demo.gearhub.local 표시가 있는 운영 데모 행과 SIMULATION 분석 이벤트를 재생성한다. LIVE 이벤트는 삭제하지 않는다.

## 4. 자동 검증 범위

| 대상 | 검증 |
|---|---|
| Member | 애플리케이션·그룹 멤버십 |
| Asset | 자산 기본값, 재고 차감·복구, 입고 |
| Request | 상태 전이, 기간, 권한, 하위 서비스 연결 |
| Budget | 예산 승인·반려와 이벤트 |
| Demand Analytics | 시간순 분할, 4주 예측, 이벤트 변환, 저장 |
| Vue | production build와 production dependency audit |

Spring 테스트는 호스트에서 실행하므로 MariaDB와 Kafka 주소를 localhost로 지정한다.

~~~powershell
$env:SPRING_DATASOURCE_URL='jdbc:mariadb://localhost:3379/lecture_db'
$env:SPRING_DATASOURCE_USERNAME='manager'
$env:SPRING_DATASOURCE_PASSWORD='SqlDba-1'
$env:SPRING_KAFKA_BOOTSTRAP_SERVERS='localhost:9092'
.\gradlew.bat test --no-daemon
~~~

위 명령은 user-service, course-service, enrollment-service, payment-service에서 각각 실행한다.

Analytics:

~~~powershell
docker compose exec -T alternative-service python -m pytest -q
~~~

Frontend:

~~~powershell
Set-Location vue-frontend
npm ci
npm run build
npm audit --omit=dev
~~~

## 5. 발표 직전 Smoke Test

~~~powershell
docker compose ps
Invoke-WebRequest http://localhost:3000 -UseBasicParsing
Invoke-WebRequest http://localhost:8761/actuator/health -UseBasicParsing
~~~

수동 확인:

- http://localhost:3000에 GearHub Campus가 표시되는가
- 관리자 계정 로그인 후 /groups가 열리는가
- /groups/1에서 자산과 관리자 메뉴가 보이는가
- /groups/1/loans에 대여 상태가 보이는가
- /groups/1/admin에 대여·도입 검토가 보이는가
- /groups/1/analytics에 평가와 7개 카테고리가 보이는가

## 6. 5분 데모 시나리오

### 0:00~0:35 문제와 제품

“학교 공용 장비와 학과·동아리 장비가 흩어져 있어 대여부터 반납, 도입까지 추적하기 어렵다”는 문제를 설명한다. 그룹별 서버가 아니라 하나의 동적 워크스페이스를 사용한다는 결정을 함께 말한다.

### 0:35~1:10 그룹과 자산

관리자 계정으로 로그인해 8개 그룹을 보여준다. 컴퓨터공학과에서 학교 공용 자산과 그룹 전용 자산, 총수량·가용수량·픽업 위치·최대 대여일을 보여준다.

### 1:10~2:20 대여와 반납

구성원이 자산을 신청하고 관리자가 승인해 가용 수량이 감소하는 것을 보여준다. 구성원의 반납 요청만으로는 수량이 오르지 않으며, 관리자가 실물을 확인한 뒤 복구된다는 점을 강조한다.

### 2:20~3:15 도입과 예산

미보유 장비 도입 요청을 만든다. 그룹 승인 → 학교 예산 승인 → 입고 → 대여 가능한 자산 전환 흐름을 상태와 서비스 기준으로 설명한다.

### 3:15~4:35 수요예측

먼저 기준선 WAPE 70.3414%와 모델 WAPE 55.7159%를 보여준다. 다음으로 4주 예상 수요, 필요 수량, 현재 재고, 부족 수량과 다른 그룹 이동 제안을 보여준다.

### 4:35~5:00 결론

“생성형 AI API를 붙인 것이 아니라 대여 이벤트를 시간순으로 학습하고 단순 기준선과 비교해, 재고 이동과 도입 판단으로 연결했다”로 마무리한다.

## 7. 기술 데모에서 추가할 내용

발표 시간이 10분 이상이면 다음을 추가한다.

1. Eureka의 7개 애플리케이션 등록 상태
2. Member·Asset·Request·Budget·Analytics의 데이터 소유권
3. payment.completed와 rental.lifecycle 이벤트
4. 운영 200건과 분석 10,814건을 분리한 이유
5. Poisson, Random Forest, Histogram Gradient Boosting의 검증 비교
6. peak 수요와 평균 대여기간으로 필요 수량을 계산하는 방식
7. 분산 트랜잭션과 합성 데이터의 현재 한계

## 8. 화면 증빙 목록

실제 제출 이미지가 필요하면 docs/evidence/screenshots에 다음 파일명으로 저장한다.

| 파일명 | 내용 |
|---|---|
| 01-groups.png | 8개 그룹 목록 |
| 02-group-dashboard.png | 그룹 요약과 권한 메뉴 |
| 03-assets-before.png | 대여 승인 전 자산 수량 |
| 04-loan-active.png | 승인된 대여와 감소한 수량 |
| 05-return-requested.png | 반납 요청, 수량 유지 |
| 06-returned.png | 반납 확인과 수량 복구 |
| 07-acquisition.png | 도입 요청 상태 |
| 08-budget.png | 학교 예산 검토 |
| 09-analytics-evaluation.png | 기준선과 모델 평가 |
| 10-analytics-forecast.png | 부족·이동 제안 |
| 11-compose.png | 11개 컨테이너 |
| 12-eureka.png | 서비스 등록 |

현재 저장소에는 캡처 파일이 아직 없으므로 제출 전 TEAM INPUT으로 실제 이미지를 추가해야 한다. 토큰, 비밀번호, client secret은 캡처하지 않는다.

## 9. 실패 시 점검 순서

1. docker compose ps로 중지된 서비스가 있는지 본다.
2. Auth, Eureka, MariaDB, Kafka의 health 상태를 확인한다.
3. Gateway와 대상 서비스 로그를 함께 확인한다.
4. 브라우저 sessionStorage의 현재 groupId와 로그인 토큰을 확인한다.
5. 데이터가 꼬였으면 데모 시드를 재실행한다.
6. AI 결과가 없으면 이벤트 수를 확인한 뒤 train API를 실행한다.

## 10. 데모에서 숨기지 않을 한계

- 한 학교 안의 멀티그룹이며 다학교 테넌시는 아니다.
- 미래 날짜가 겹치는 예약 수량을 별도 계산하지 않는다.
- 시리얼별 손상·수리·분실과 실제 발주·배송은 없다.
- AI 수치는 합성 이력으로 파이프라인과 비교 방법을 검증한 결과다.
- 분산 트랜잭션 보상, 내부 서비스 인증, 감사로그는 제품화 과제다.
