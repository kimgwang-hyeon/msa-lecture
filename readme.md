# SKALA GearHub

교육기관과 기업이 내부 교보재를 **찾고, 빌리고, 구매 요청**할 수 있도록 만든 B2B 자산 운영 서비스입니다.

기존 MSA 실습 구조를 유지하면서 다음 흐름을 구현했습니다.

```text
교보재 조회 → 대여 신청 → 운영진 승인 → 재고 차감
신규 장비 요청 → 대체재 추천 → 예산 승인·반려 → 신청 상태 반영
```

> 이 프로젝트는 2일 애자일·MSA 실습용 MVP입니다. 실제 결제는 발생하지 않습니다.

## 1. 준비물

- Docker Desktop
- Node.js 20 이상
- Git
- 팀에서 별도로 전달받은 `infra-images.tar`

`infra-images.tar`에는 제공받은 Auth Server와 API Gateway 이미지가 들어 있습니다. 용량이 커서 GitHub에는 올리지 않았습니다.

## 2. 처음 한 번만 실행

### 저장소 내려받기

```bash
git clone https://github.com/kimgwang-hyeon/msa-lecture.git
cd msa-lecture
```

### Docker 이미지 불러오기

`infra-images.tar`를 프로젝트 루트에 넣은 뒤 실행합니다.

```bash
docker load -i infra-images.tar
```

### 백엔드 실행

```bash
docker compose up -d --build
```

처음 실행할 때는 서비스 빌드 때문에 시간이 조금 걸릴 수 있습니다.

상태 확인:

```bash
docker compose ps
```

모든 서비스가 `Up` 또는 `healthy`가 될 때까지 잠시 기다립니다.

## 3. 프론트엔드 실행

새 터미널을 열고 프로젝트 루트에서 실행합니다.

```bash
cd vue-frontend
npm ci
cp .env.example .env
npm run dev
```

브라우저에서 아래 주소로 접속합니다.

<http://localhost:3000>

`.env`의 `VITE_CLIENT_ID`와 `VITE_CLIENT_SECRET`은 팀에서 전달받은 Auth Server 클라이언트 값으로 입력합니다. 실제 `.env`는 GitHub에 올리지 않습니다.

## 4. 가장 간단한 테스트

1. 회원가입에서 교육생 계정을 만듭니다.
2. 운영진 계정도 하나 만듭니다.
3. 교육생으로 로그인해 교보재를 선택하고 대여 신청합니다.
4. 운영진으로 다시 로그인해 `승인 관리`에서 대여를 승인합니다.
5. 교육생 화면에서 신청 상태가 `대여 승인`으로 바뀌고 재고가 감소했는지 확인합니다.
6. `신규 교보재 신청`에서 상품명·가격·수량·구매 링크를 입력합니다.
7. 운영진의 `예산 검토`에서 승인 또는 반려하고 결과를 확인합니다.

## 5. 주요 주소

| 기능 | 주소 |
|---|---|
| 프론트엔드 | <http://localhost:3000> |
| API Gateway | <http://localhost:8080> |
| Eureka | <http://localhost:8761> |
| Asset Service | <http://localhost:8082> |
| Request Service | <http://localhost:8083> |
| Budget Service | <http://localhost:8084> |

프론트엔드는 개별 서비스가 아니라 API Gateway(`8080`)를 통해 백엔드를 호출합니다.

## 6. 자주 사용하는 명령

```bash
# 전체 로그 확인
docker compose logs -f

# 특정 서비스 로그 확인
docker compose logs -f request-service

# 백엔드 종료
docker compose down

# 프론트엔드 종료
# npm run dev를 실행한 터미널에서 Ctrl+C
```

## 7. 문제가 생겼을 때

### 포트가 이미 사용 중이라고 나올 때

이미 실행 중인 Docker 서비스를 먼저 확인합니다.

```bash
docker compose ps
```

기존 프로젝트를 종료한 뒤 다시 실행합니다.

```bash
docker compose down
docker compose up -d --build
```

### 로그인 화면에서 오류가 날 때

- 백엔드 컨테이너가 모두 실행 중인지 확인합니다.
- `vue-frontend/.env`가 존재하는지 확인합니다.
- `VITE_CLIENT_ID`, `VITE_CLIENT_SECRET`, `VITE_REDIRECT_URI` 값을 확인합니다.
- 토큰이 만료되면 로그아웃 후 다시 로그인합니다.

### 팀원 간 공유 시 주의

- `node_modules`, `build`, `dist`는 공유하지 않고 각자 다시 생성합니다.
- `infra-images.tar`는 GitHub가 아닌 별도 파일 공유로 전달합니다.
- `.env`에는 실제 인증 정보가 들어갈 수 있으므로 저장소에 올리지 않습니다.

## 문서

- [팀 공유 실행 안내](docs/SHARING_GUIDE.md)
- [B2B SaaS 기획·도메인](docs/agile/02_PRODUCT_AND_DOMAIN.md)
- [Product Backlog](docs/agile/03_PRODUCT_BACKLOG.md)
- [2일 Sprint 계획](docs/agile/04_TWO_DAY_SPRINT_PLAN.md)
- [아키텍처·ERD](docs/agile/05_ARCHITECTURE_AND_ERD.md)
- [API 계약](docs/agile/06_API_CONTRACT.md)
- [검증·데모 체크리스트](docs/agile/07_VALIDATION_AND_DEMO.md)
