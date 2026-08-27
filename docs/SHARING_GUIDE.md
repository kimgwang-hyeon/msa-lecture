# GearHub Campus 팀 공유 안내

## 저장소에 포함할 것

- 업무 서비스: `user-service`, `course-service`, `enrollment-service`, `payment-service`, `recommend-service`
- 인프라 소스와 설정: `eureka-server`, `init-db`, `docker-compose.yml`
- 프론트엔드 소스: `vue-frontend`
- 실행·설계·검증 문서: `readme.md`, `docs`

## 저장소에서 제외할 것

- Gradle `build/`
- `node_modules/`, `dist/`
- 로컬 `.env`
- `infra-images.tar`와 기타 대용량 압축 파일
- 개인 토큰·비밀번호·인증서

`infra-images.tar`는 약 343MB의 제공 인프라 이미지 묶음이므로 Git이 아닌 팀 파일 공유 공간으로 전달한다.

## 새 PC에서 실행

1. 저장소와 `infra-images.tar`를 준비한다.
2. Docker Desktop을 실행한다.
3. 프로젝트 루트에서 다음을 실행한다.

```powershell
docker load -i infra-images.tar
docker compose up -d --build
docker compose exec -T alternative-service python scripts/seed_demo_data.py
docker compose ps
```

Compose가 백엔드, 인프라, 프론트엔드를 모두 올린다. 브라우저에서 <http://localhost:3000>을 연다.

프론트 Docker 빌드는 실습 Auth Server에 맞는 `web-client / web-secret` 기본값을 갖고 있다. 다른 OAuth 클라이언트를 사용하는 Docker 이미지는 Compose build args로 값을 덮어쓴다. `vue-frontend/.env`는 아래 로컬 개발 서버에서만 사용하며 Docker 이미지에는 복사하지 않는다.

로컬 개발 서버가 필요한 경우에만 다음을 실행한다.

```powershell
cd vue-frontend
npm ci
Copy-Item .env.example .env
npm run dev
```

## 데모 상태 복구

발표 전에 아래 명령을 다시 실행하면 데모용 자산·대여·도입 요청과 예측 결과가 고정 시드 상태로 돌아간다.

```powershell
docker compose exec -T alternative-service python scripts/seed_demo_data.py
```

이 명령은 GearHub 데모 표시가 붙은 운영 데이터와 `SIMULATION` 분석 이벤트를 교체한다. 실제 Kafka로 수집한 `LIVE` 분석 이벤트는 삭제하지 않는다.

데모 계정:

- 관리자: `campus.admin@demo.local / GearHub123!`
- 구성원: `campus.member@demo.local / GearHub123!`

시연 전용 계정이므로 공개·운영 환경에서는 제거하거나 비밀번호를 교체한다.

## 최소 검증

```powershell
docker compose ps
docker compose exec -T alternative-service python -m pytest -q

cd vue-frontend
npm ci
npm run build
```

최종 전체 검증 결과와 Spring 테스트 환경변수는 [agile/10_FINAL_VALIDATION.md](./agile/10_FINAL_VALIDATION.md)에 기록되어 있다.

## 주의

- `docker compose down`은 볼륨을 유지하지만 `down -v`는 DB와 Kafka 데이터를 지운다.
- `node_modules`, `build`, `dist`를 압축해 공유하지 말고 각 PC에서 다시 만든다.
- 제공 Auth/Gateway 이미지는 팀이 함께 전달받아야 하며 GitHub에 올리지 않는다.
- 실습용 DB 비밀번호와 OAuth 클라이언트 값은 운영용 비밀정보로 재사용하지 않는다.
