# GearHub Campus 최종 검증 기록

검증 기준일: 2026-08-27 (Asia/Seoul)

이 문서는 구현했다고 추정한 항목이 아니라 로컬 Docker 환경에서 실제로 실행해 확인한 결과를 기록한다. 최종 시드는 `seed=42`를 사용했다.

## 1. 실행 상태

`docker compose up -d --build` 후 다음 11개 컨테이너가 모두 실행 중임을 확인했다.

| 구분 | 컨테이너 | 공개 포트 |
|---|---|---:|
| Web | `gearhub-frontend` | 3000 |
| Gateway | `lecture-gateway` | 8080 |
| Auth | `lecture-auth` | 9000 |
| Discovery | `lecture-eureka` | 8761 |
| Member | `gearhub-member` | 8081 |
| Asset | `gearhub-asset` | 8082 |
| Request | `gearhub-request` | 8083 |
| Budget | `gearhub-budget` | 8084 |
| Analytics | `gearhub-alternative` | 8085 |
| Kafka | `lecture-kafka` | 9092 |
| MariaDB | `lecturedb` | 3379 |

확인 명령:

```powershell
docker compose ps
```

## 2. 재현 가능한 데모 데이터

```powershell
docker compose exec -T alternative-service python scripts/seed_demo_data.py
```

최종 출력:

```text
운영 데이터: groups=8, assets=120, loans=200, acquisitions=8
분석 데이터: events=10814, model=hist_gradient_boosting
기준선 WAPE=70.3414, 모델 WAPE=55.7159
```

DB 검증 결과:

| 항목 | 결과 |
|---|---:|
| 활성 그룹 | 8 |
| 대여 가능 데모 자산 | 120 |
| 운영 화면용 대여 요청 | 200 |
| 도입 요청 | 8 |
| 시뮬레이션 분석 이벤트 | 10,814 |
| Kafka로 수집한 LIVE 이벤트 | 1 이상 |
| 4주 예측 행 | 224 |
| 음수 또는 총수량 초과 재고 | 0 |

시드 스크립트는 자신이 만든 `https://demo.gearhub.local/` 표시 데이터만 교체한다. 분석 이력은 `SIMULATION` 소스만 삭제하므로 운영 중 수집한 `LIVE` 이벤트는 남는다.

## 3. AI 평가 결과

| 항목 | 결과 |
|---|---:|
| 데이터 기간 | 2025-03-03 ~ 2026-08-24 |
| 이벤트 수 | 10,814 |
| 학습 행 | 3,248 |
| 테스트 행 | 672 |
| 선택 모델 | Histogram Gradient Boosting |
| 기준선 MAE | 1.4717 |
| 모델 MAE | 1.1657 |
| 기준선 WAPE | 70.3414% |
| 모델 WAPE | 55.7159% |
| WAPE 개선율 | 20.79% |

컴퓨터공학과 화면에서는 7개 카테고리 중 3개가 부족으로 계산되었고, 부속품·전자/IoT·메이커 장비에 대해 다른 그룹에서 먼저 이동할 수 있는 수량이 함께 표시됐다. 즉 모델 결과가 단순 차트로 끝나지 않고 재고 이동 또는 도입 검토라는 관리자 행동으로 연결되는 것을 확인했다.

## 4. 업무 흐름 통합 검증

### 대여·반납

임시 요청을 생성해 다음 상태와 재고 변화를 실제 API로 확인했다.

```text
신청 PENDING
→ 그룹 관리자 승인 ACTIVE: 가용 재고 1 감소
→ 구성원 반납 요청 RETURN_REQUESTED: 재고 변화 없음
→ 관리자 반납 확인 RETURNED: 가용 재고 1 복구
```

검증 요청 ID는 테스트 시점의 `225`였으며, 최종 시드 재실행으로 운영 데모 데이터는 다시 200건으로 정리했다.

### 도입·예산·입고

임시 도입 요청으로 다음 서비스 연결을 확인했다.

```text
Request PENDING
→ 그룹 승인 GROUP_APPROVED
→ Budget COMPLETED
→ Kafka payment.completed
→ Request BUDGET_APPROVED
→ 입고 RECEIVED
→ Asset OWNED / ACTIVE, 가용 수량 2
```

검증 요청 ID는 테스트 시점의 `409`였다. 이 데이터도 최종 시드 재실행으로 정리했다.

### Kafka 분석 이벤트

대여 요청 이벤트의 Java `LocalDate / LocalDateTime` 배열 직렬화 형식을 Analytics Consumer가 날짜로 변환하는 것을 확인했다. 임시 요청 `417`의 `REQUESTED` 이벤트가 `analytics_loan_events.source='LIVE'`로 1건 저장됐고 Consumer 로그에 오류가 없었다.

## 5. 자동 테스트와 빌드

### Spring 서비스

Docker의 MariaDB와 Kafka를 사용하도록 로컬 포트를 지정한 뒤 Member, Asset, Request, Budget 네 서비스의 전체 Gradle 테스트가 모두 성공했다.

```powershell
$env:SPRING_DATASOURCE_URL='jdbc:mariadb://localhost:3379/lecture_db'
$env:SPRING_DATASOURCE_USERNAME='manager'
$env:SPRING_DATASOURCE_PASSWORD='SqlDba-1'
$env:SPRING_KAFKA_BOOTSTRAP_SERVERS='localhost:9092'
.\gradlew.bat test --no-daemon
```

위 명령은 각 서비스 디렉터리에서 실행한다. Docker 내부 기본 호스트명은 `lecturedb`, `kafka`이므로 호스트 테스트에서는 환경변수 지정이 필요하다.

결과:

- Member Service: `BUILD SUCCESSFUL`
- Asset Service: `BUILD SUCCESSFUL`
- Request Service: `BUILD SUCCESSFUL`
- Budget Service: `BUILD SUCCESSFUL`

### Analytics

```powershell
docker compose exec -T alternative-service python -m pytest -q
```

결과: `4 passed`. 예측 시간순 분할·4주 출력, 시뮬레이션, Kafka 이벤트 변환, 날짜 저장을 검증한다. Pydantic·SciPy·pandas의 향후 폐기 예정 경고는 남아 있지만 테스트 실패나 런타임 오류는 아니다.

### Frontend

```powershell
cd vue-frontend
npm ci
npm run build
```

결과: 보안 패치된 Axios 1.20.0 기준으로 Vite가 125개 모듈을 변환하고 production build를 성공했다. `npm audit --omit=dev` 결과는 취약점 0건이다.

## 6. 브라우저 사용자 흐름

실제 인앱 브라우저로 다음을 확인했다.

1. `http://localhost:3000`의 문서 제목과 랜딩 브랜드가 `GearHub Campus`로 표시된다.
2. `campus.admin@demo.local` 계정으로 Auth Server 로그인에 성공한다.
3. 로그인 후 `/groups`에서 8개 그룹과 관리자 역할이 표시된다.
4. `/groups/1`에서 컴퓨터공학과, 자산 36개, 구성원 수, 초대코드가 표시된다.
5. `/groups/1/analytics`에서 모델 평가와 7개 카테고리의 4주 예측이 표시된다.
6. 부족 카테고리와 그룹 간 이동 제안이 실제 재고와 함께 렌더링된다.

최종 확인을 위해 AI 수요예측 탭을 열린 상태로 두었다.

## 7. 발표용 5분 데모 순서

1. 랜딩에서 “학교 공용 + 그룹 전용 자산” 문제를 20초 안에 설명한다.
2. 관리자 계정으로 로그인해 8개 동적 그룹을 보여준다.
3. 컴퓨터공학과 자산 카탈로그에서 보유 수량·가용 수량·대여기간을 보여준다.
4. 구성원 계정의 대여 신청을 만들고 관리자 승인 후 재고가 줄어드는 것을 보여준다.
5. 반납 요청과 관리자 확인 후 재고가 복구되는 것을 보여준다.
6. 미보유 장비 요청의 그룹 승인 → 예산 승인 → 입고 흐름을 상태 다이어그램으로 설명한다.
7. AI 화면에서 기준선과 모델 WAPE를 먼저 보여준 뒤, 부족 3개와 이동 제안을 보여준다.
8. “생성형 API를 붙인 것이 아니라 우리 대여 이력을 scikit-learn으로 비교·검증해 관리자 행동으로 연결했다”로 마무리한다.

데모 직전에는 시드 명령을 한 번 실행하면 화면 상태를 일정하게 되돌릴 수 있다.

## 8. 남은 MVP 한계

- 현재는 한 학교 안의 멀티그룹이며 여러 학교의 데이터 격리는 구현하지 않았다.
- 날짜가 겹치는 미래 예약의 수량을 별도 캘린더로 계산하지 않는다.
- 도입은 실제 발주·배송 시스템과 연결되지 않는다.
- 자산 개별 시리얼, 손상·수리·분실 처리는 없다.
- 내부 서비스 API의 별도 서비스 인증과 완전한 감사로그는 제품화 단계가 필요하다.
- 프론트 OAuth는 제공 실습 Auth Server 호환을 우선했다. 운영 제품에서는 PKCE 또는 BFF로 브라우저에 client secret을 두지 않아야 한다.
- 현재 AI 수치는 합성 이력으로 기능과 평가 방법을 검증한 결과다. 실제 운영 판단 전에는 실제 데이터로 재학습·재검증해야 한다.
