# GearHub Campus

대학교 안의 학과·연구실·동아리가 학교 공용 자산과 그룹 전용 자산을 함께 대여·반납·도입하는 B2B 자산 운영 MVP입니다. 기존 Agile MSA 실습 템플릿의 서비스 경계와 물리 테이블을 유지하면서 멀티그룹 운영과 관리자용 수요예측을 구현했습니다.

## 구현된 핵심 흐름

1. 구성원이 초대코드로 그룹에 참여합니다.
2. 학교 공용 또는 소속 그룹 자산을 기간과 사용 사유를 입력해 신청합니다.
3. 그룹 관리자가 승인하면 재고가 차감됩니다.
4. 구성원이 반납을 요청하고 관리자가 확인하면 재고가 복구됩니다.
5. 없는 장비는 도입 요청 → 그룹 승인 → 학교 예산 승인 → 입고 순서로 자산화됩니다.
6. 관리자는 18개월 대여 이력으로 계산한 다음 4주 수요, 부족 수량, 그룹 간 이동 대안을 확인합니다.

AI는 대여자에게 물건을 추천하지 않습니다. 관리자가 재고 이동·도입을 판단할 수 있도록 `그룹 × 자산 카테고리` 수요를 예측하는 데만 사용합니다.

### 기획 배경과 사용자 가치

초기에는 부트캠프 장비 신청 기능으로 시작했지만, 실제 동아리원이 겪는 대여 불편을 기준으로 신청 이후의 승인과 반납까지 범위를 확장했습니다.

- 학교 관리자는 그룹을 만들고 초대코드를 발급합니다.
- 동아리 구성원은 초대코드로 그룹에 참여해 보유 장비를 대여하고 반납합니다.
- 동아리에 없는 장비는 `동아리 도입 신청 → 그룹 검토 → 학교 예산 승인 → 입고 확인 → 정식 자산 등록` 순서로 처리합니다.
- 학교 예산 관리자는 그룹별 향후 4주 수요예측을 참고해 재고 이동과 도입 예산을 판단합니다.
- 수요예측은 최근 4주 이동평균을 기준선으로 두고 여러 후보를 비교하며, 제공 시드에서는 Scikit-learn의 Histogram Gradient Boosting이 선택됩니다.

## 서비스 구성

| 논리 서비스 | 포트 | 책임 |
|---|---:|---|
| Member Service | 8081 | 사용자, 그룹, 초대코드, 그룹 역할 |
| Asset Service | 8082 | 학교·그룹 자산, 수량 재고, 입고 |
| Request Service | 8083 | 대여·반납·도입 요청 상태 |
| Budget Service | 8084 | 학교 예산 승인·반려 |
| Demand Analytics Service | 8085 | Kafka 이력 수집, 모델 학습, 4주 예측 |
| API Gateway | 8080 | OAuth2 검증과 업무 API 라우팅 |
| Frontend | 3000 | 그룹별 동적 워크스페이스 |

공통 인프라는 Auth Server(9000), Eureka(8761), Kafka(9092), MariaDB(3379)를 사용합니다.

## 바로 실행

필요한 것은 Docker Desktop입니다. 제공 인프라 이미지가 로컬에 없다면 먼저 팀에서 받은 `infra-images.tar`를 프로젝트 루트에 놓고 불러옵니다.

프론트엔드는 실행 전에 예제 환경설정을 그대로 복사해 `.env`를 만듭니다.

```powershell
Copy-Item vue-frontend/.env.example vue-frontend/.env
```

macOS 또는 Linux에서는 다음 명령을 사용합니다.

```bash
cp vue-frontend/.env.example vue-frontend/.env
```

예제에는 API Gateway, Auth Server, OAuth 클라이언트와 콜백 주소가 로컬 Docker Compose 환경에 맞게 들어 있습니다. 팀 공유용 로컬 테스트에서는 값을 수정하지 않아도 됩니다.

```powershell
docker load -i infra-images.tar
docker compose up -d --build
docker compose exec -T alternative-service python scripts/seed_demo_data.py
docker compose ps
```

브라우저에서 <http://localhost:3000>을 엽니다. Compose가 프론트엔드까지 함께 올리므로 별도 `npm run dev`는 필요하지 않습니다.

### 데모 계정

| 역할 | 이메일 | 비밀번호 |
|---|---|---|
| 학교·그룹 관리자 | `campus.admin@demo.local` | `GearHub123!` |
| 일반 구성원 | `campus.member@demo.local` | `GearHub123!` |

위 계정은 시연 전용입니다. 운영 환경에서는 기본 계정과 OAuth 클라이언트 값을 반드시 교체해야 합니다.

시드 명령은 반복 실행할 수 있으며 다음 데이터를 다시 만듭니다.

- 활성 그룹 8개
- 대여 가능 자산 120개
- 운영 화면용 대여 요청 200건
- 단계별 도입 요청 8건
- 분석용 78주 시뮬레이션 이벤트 10,814건
- 8개 그룹 × 7개 카테고리 × 4주 예측 224건

운영 화면 데이터와 모델 학습용 시뮬레이션 이력은 분리되어 있어 10,814건이 내 요청 화면에 쏟아지지 않습니다. 실제 요청 이벤트는 Kafka를 통해 `LIVE` 소스로 별도 축적됩니다.

## 로컬 통합 테스트 시나리오

### 1. 실행 상태와 데이터셋 확인

```powershell
docker compose ps
docker compose exec -T alternative-service python scripts/seed_demo_data.py
```

확인 기준:

- 주요 컨테이너가 `Up`, healthcheck가 있는 서비스는 `healthy` 상태입니다.
- 시드 결과에 그룹 8개, 자산 120개, 대여 요청 200건, 도입 요청 8건, 분석 이벤트 10,814건이 표시됩니다.
- 브라우저에서 <http://localhost:3000>이 열립니다.

### 2. 일반 구성원, 대여와 반납

1. `campus.member@demo.local`로 로그인합니다.
2. 초대코드를 사용하는 그룹 참여 화면과 소속 그룹 목록을 확인합니다.
3. 소속 그룹에서 재고가 있는 자산을 선택하고 대여를 신청합니다.
4. 요청 상태가 승인 대기로 표시되는지 확인합니다.
5. 관리자 승인 후 상태가 대여 중으로 바뀌고 가용 재고가 1개 감소하는지 확인합니다.
6. 구성원이 반납을 요청한 뒤 관리자가 반납을 확인하면 재고가 1개 복원되는지 확인합니다.

### 3. 일반 구성원, 미보유 장비 도입 신청

1. 일반 구성원 계정에서 그룹에 없는 장비의 도입을 신청합니다.
2. 장비명, 필요 수량, 예상 금액, 필요 사유가 관리자 화면에 전달되는지 확인합니다.
3. 그룹 승인 후 요청이 학교 예산 검토 단계로 이동하는지 확인합니다.

### 4. 관리자, 예산 승인과 입고

1. `campus.admin@demo.local`로 로그인합니다. (비밀번호 : GearHub123!)
2. 운영 데스크에서 대여, 반납, 도입, 예산 요청을 확인합니다.
3. 도입 요청을 승인하고 학교 예산을 승인합니다.
4. Kafka의 `payment.completed` 이벤트 처리 후 요청이 예산 승인 상태로 바뀌는지 확인합니다.
5. 입고 수량과 수령 장소를 입력해 입고를 완료합니다.
6. 해당 장비가 정식 보유 자산으로 전환되고 총수량과 가용수량에 입고 수량이 반영되는지 확인합니다.

Kafka 처리 상태는 다음 로그로 확인할 수 있습니다.

```powershell
docker compose logs --tail=200 request-service budget-service alternative-service kafka
```

### 5. 관리자, 향후 4주 수요예측

1. 관리자 계정으로 AI 수요예측 화면을 엽니다.
2. 그룹과 카테고리별 4주 예상 신청 수요가 표시되는지 확인합니다.
3. 부족 예상 수량, 현재 재고, 다른 그룹의 이동 가능 수량이 함께 표시되는지 확인합니다.
4. 모델 상세에서 선택 모델이 `Histogram Gradient Boosting`인지 확인합니다.
5. 기준선 WAPE `70.3414%`, 모델 WAPE `55.7159%`, 개선율 `20.79%`가 표시되는지 확인합니다.

### 6. 자동 테스트

```powershell
# Demand Analytics 테스트
docker compose exec -T alternative-service python -m pytest -q

# Frontend 빌드와 단위 테스트
Set-Location vue-frontend
npm ci
npm run build
npm run test:unit
```

수동 테스트를 다시 시작하려면 시드 명령을 재실행합니다. `docker compose down -v`는 DB와 Kafka 볼륨의 데이터를 삭제하므로 데이터 초기화가 꼭 필요한 경우에만 사용합니다.

## 현재 모델 결과

고정 시드 `42`로 학습하면 시간순 학습·검증·테스트 분할을 거쳐 Histogram Gradient Boosting이 선택됩니다.

| 지표 | 최근 4주 이동평균 기준선 | 선택 모델 |
|---|---:|---:|
| MAE | 1.4717 | 1.1657 |
| WAPE | 70.3414% | 55.7159% |

기준선 대비 WAPE 개선율은 20.79%입니다. 화면에는 성능 수치뿐 아니라 예상 수요, 평균 대여기간, 현재 재고를 결합한 부족 수량과 우선 이동 그룹이 표시됩니다.

## 개발 검증

```powershell
# 프론트 정적 빌드
cd vue-frontend
npm ci
npm run build

# 분석 단위 테스트
cd ..
docker compose exec -T alternative-service python -m pytest -q
```

Spring 컨텍스트 테스트는 MariaDB와 Kafka가 실행된 상태에서 로컬 포트(3379, 9092)를 지정해 검증했습니다. 자세한 명령과 최종 결과는 [최종 검증 기록](docs/agile/10_FINAL_VALIDATION.md)에 있습니다.

## 자주 쓰는 명령

```powershell
docker compose ps
docker compose logs -f request-service alternative-service
docker compose exec -T alternative-service python scripts/seed_demo_data.py
docker compose down
```

`docker compose down`은 컨테이너만 내리고 DB 볼륨은 유지합니다. 볼륨 삭제는 데모 데이터를 모두 지워도 되는 경우에만 별도로 수행하세요.

## 문서

- [최종 제품 전환·AI 설계](docs/agile/09_CAMPUS_PIVOT_AND_AI.md)
- [최종 검증·데모 시나리오](docs/agile/10_FINAL_VALIDATION.md)
- [전체 실습 문서 인덱스](docs/agile/00_DOCUMENT_INDEX.md)
- [팀 공유 실행 안내](docs/SHARING_GUIDE.md)
- [현재 서비스 상세 설명](docs/SKALA_GEARHUB.md)
