# GearHub Campus 최종 검증 기록

검증 기준일: 2026-08-27 (Asia/Seoul)

이 문서는 현재 브랜치의 Docker 환경과 고정 seed 42에서 실제로 확인한 결과다.

## 1. 실행 상태

docker compose ps 재확인 결과 11개 컨테이너가 모두 running 상태다.

| 구분 | Compose 서비스 | 컨테이너 | 공개 포트 |
|---|---|---|---:|
| Web | frontend | gearhub-frontend | 3000 |
| Gateway | api-gateway | lecture-gateway | 8080 |
| Auth | auth-server | lecture-auth | 9000 |
| Discovery | eureka-server | lecture-eureka | 8761 |
| Member | member-service | gearhub-member | 8081 |
| Asset | asset-service | gearhub-asset | 8082 |
| Request | request-service | gearhub-request | 8083 |
| Budget | budget-service | gearhub-budget | 8084 |
| Demand Analytics | alternative-service | gearhub-alternative | 8085 |
| Kafka | kafka | lecture-kafka | 9092 |
| MariaDB | mariadb | lecturedb | 3379 |

확인 명령:

~~~powershell
docker compose ps
~~~

Eureka에서 다음 7개 애플리케이션이 UP으로 등록된 것을 확인했다.

- ALTERNATIVE-SERVICE
- API-GATEWAY
- ASSET-SERVICE
- AUTH-SERVER
- BUDGET-SERVICE
- MEMBER-SERVICE
- REQUEST-SERVICE

## 2. 재현 가능한 시드

~~~powershell
docker compose exec -T alternative-service python scripts/seed_demo_data.py
~~~

기준 출력:

~~~text
운영 데이터: groups=8, assets=120, loans=200, acquisitions=8
분석 데이터: events=10814, model=hist_gradient_boosting
기준선 WAPE=70.3414, 모델 WAPE=55.7159
~~~

DB 재확인 결과:

| 항목 | 결과 |
|---|---:|
| 활성 그룹 | 8 |
| 데모 OWNED 자산 | 120 |
| 운영 LOAN 요청 | 200 |
| PURCHASE 도입 요청 | 8 |
| SIMULATION 분석 이벤트 | 10,814 |
| LIVE 분석 이벤트 | 1 |
| 최신 실행의 4주 예측 행 | 224 |
| availableQuantity가 0 미만인 자산 | 0 |
| availableQuantity가 totalQuantity를 넘는 자산 | 0 |

시드는 demo.gearhub.local 표시 데이터를 대상으로 하며 SIMULATION만 교체한다. Kafka로 수집된 LIVE 이벤트는 보존한다.

## 3. AI 평가

최신 analytics_forecast_runs 행:

| 항목 | 결과 |
|---|---:|
| 데이터 기간 | 2025-03-03 ~ 2026-08-24 |
| 이벤트 수 | 10,814 |
| 학습·개발 행 | 3,248 |
| 테스트 행 | 672 |
| 선택 모델 | hist_gradient_boosting |
| 기준선 MAE | 1.4717 |
| 모델 MAE | 1.1657 |
| 기준선 WAPE | 70.3414% |
| 모델 WAPE | 55.7159% |
| WAPE 개선율 | 20.79% |

Poisson, Random Forest, Histogram Gradient Boosting을 시간순 검증 구간에서 비교한 뒤 Histogram Gradient Boosting을 선택했다. 마지막 테스트 구간에서 최근 4주 이동평균 기준선보다 MAE와 WAPE가 모두 낮았다.

컴퓨터공학과 예측 화면에서는 7개 카테고리와 4주 주간값이 표시되고, 부족 카테고리에는 다른 그룹의 계획상 이동 가능 수량이 함께 표시됐다.

## 4. 대여·반납 통합 검증

실제 API로 다음 상태와 재고 변화를 확인했다.

~~~text
대여 신청 PENDING
→ 그룹 관리자 승인 ACTIVE
→ availableQuantity 1 감소
→ 구성원 반납 요청 RETURN_REQUESTED
→ availableQuantity 변화 없음
→ 관리자 반납 확인 RETURNED
→ availableQuantity 1 복구
~~~

추가로 확인한 실패 조건:

- 다른 그룹의 GROUP 자산 대여 거부
- 가용 수량 0인 자산 승인 거부
- 최대 대여일 초과 거부
- 본인이 아닌 대여의 반납 요청 거부
- RETURN_REQUESTED가 아닌 요청의 반납 확인 거부

검증용 임시 행은 최종 시드 재실행으로 데모 기준 200건에 맞췄다.

## 5. 도입·예산·입고 통합 검증

실제 서비스와 Kafka를 통해 다음 흐름을 확인했다.

~~~text
Request PURCHASE / PENDING
→ Asset PURCHASE_REQUEST / INACTIVE
→ 그룹 승인 GROUP_APPROVED
→ Budget PENDING
→ 학교 예산 승인 COMPLETED
→ Kafka payment.completed
→ Request BUDGET_APPROVED
→ 그룹 관리자 입고 확인 RECEIVED
→ Asset OWNED / ACTIVE
→ totalQuantity와 availableQuantity에 입고 수량 반영
~~~

예산 반려 시 Budget가 FAILED가 되고 payment.completed 이벤트를 받은 Request가 REJECTED로 바뀌는 계약은 자동 테스트와 코드 경로로 확인했다.

## 6. Kafka 분석 이벤트

Request의 rental.lifecycle 이벤트가 Demand Analytics에 저장되는 것을 확인했다.

- source: LIVE
- eventId 기본키 기반 upsert
- Java LocalDate와 LocalDateTime의 배열 직렬화 형식 변환
- REQUESTED 이벤트의 groupId, category, quantity, loanDays 저장
- Consumer 로그 오류 없음

현재 DB에 LIVE 이벤트 1건이 남아 있어 시드 재실행 후에도 운영 이력이 보존됨을 확인했다.

## 7. 자동 테스트와 빌드

### Spring 서비스

호스트 테스트용 환경변수:

~~~powershell
$env:SPRING_DATASOURCE_URL='jdbc:mariadb://localhost:3379/lecture_db'
$env:SPRING_DATASOURCE_USERNAME='manager'
$env:SPRING_DATASOURCE_PASSWORD='SqlDba-1'
$env:SPRING_KAFKA_BOOTSTRAP_SERVERS='localhost:9092'
.\gradlew.bat test --no-daemon
~~~

각 서비스 디렉터리에서 실행한 결과:

| 서비스 | 결과 |
|---|---|
| user-service / Member | BUILD SUCCESSFUL |
| course-service / Asset | BUILD SUCCESSFUL |
| enrollment-service / Request | BUILD SUCCESSFUL |
| payment-service / Budget | BUILD SUCCESSFUL |

### Demand Analytics

~~~powershell
docker compose exec -T alternative-service python -m pytest -q
~~~

결과: 4 passed.

검증 범위는 시간순 분할과 4주 출력, 시뮬레이션, Kafka 이벤트 변환, 날짜 저장이다. Pydantic·SciPy·pandas의 향후 폐기 예정 경고는 테스트 실패가 아니다.

### Frontend

~~~powershell
Set-Location vue-frontend
npm ci
npm run build
npm audit --omit=dev
~~~

결과:

- Vite production build 성공
- 125개 모듈 변환
- Axios 1.20.0
- production dependency 취약점 0건

## 8. 브라우저 사용자 흐름

실제 브라우저에서 확인한 항목:

1. http://localhost:3000의 제목과 랜딩 브랜드가 GearHub Campus다.
2. campus.admin@demo.local로 Auth Server 로그인이 된다.
3. /groups에서 8개 그룹과 관리자 역할이 보인다.
4. /groups/1에서 컴퓨터공학과 정보와 자산·구성원·초대코드가 보인다.
5. /groups/1/assets에서 공용·그룹 자산과 수량이 보인다.
6. /groups/1/admin에서 대여와 도입 요청을 검토할 수 있다.
7. /groups/1/analytics에서 평가 지표와 7개 카테고리의 4주 예측이 보인다.
8. 부족 수량과 그룹 간 이동 제안이 재고 정보와 함께 렌더링된다.

## 9. 데모 계정

| 역할 | 이메일 | 비밀번호 |
|---|---|---|
| 학교·그룹 관리자 | campus.admin@demo.local | GearHub123! |
| 일반 구성원 | campus.member@demo.local | GearHub123! |

이 계정은 로컬 교육용 데이터에만 사용한다.

## 10. 최종 판정

| 영역 | 판정 |
|---|---|
| 한 학교·멀티그룹 제품 흐름 | PASS |
| 대여·반납 재고 정합성 | PASS |
| 도입·예산·입고 MSA 연결 | PASS |
| Kafka 이벤트 연결 | PASS |
| 관리자 수요예측과 기준선 비교 | PASS |
| Docker 재현성 | PASS |
| 자동 테스트와 프론트 빌드 | PASS |
| 제출용 화면 이미지 | TEAM INPUT |

## 11. 남은 MVP 한계

- 현재는 한 학교 안의 멀티그룹이며 여러 학교의 데이터 격리는 없다.
- 날짜가 겹치는 미래 예약의 수량을 캘린더 단위로 계산하지 않는다.
- 도입은 실제 발주·배송·회계 시스템과 연결되지 않는다.
- 자산 개별 시리얼, 손상·수리·분실 처리가 없다.
- 원격 서비스 호출과 로컬 DB 작업 사이의 Saga·보상 트랜잭션이 없다.
- 내부 API의 서비스 인증과 완전한 감사로그가 필요하다.
- 브라우저 OAuth는 제공 Auth Server 호환을 우선했으며 운영에서는 PKCE 또는 BFF가 필요하다.
- AI 평가는 합성 이력 기반이다. 실제 운영 판단 전 LIVE 데이터로 재학습·재검증해야 한다.
