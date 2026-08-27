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
