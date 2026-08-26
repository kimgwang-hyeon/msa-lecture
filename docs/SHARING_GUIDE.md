# 조원 공유용 안내

## Git에 올릴 항목

- 업무 서비스 소스: `user-service`, `course-service`, `enrollment-service`, `payment-service`, `recommend-service`
- 인프라 소스: `eureka-server`
- 프론트엔드: `vue-frontend`의 소스와 `package.json`, `package-lock.json`
- 데이터 초기화: `init-db`
- 실행 설정: `docker-compose.yml`
- 프로젝트 설명과 Agile 문서: `readme.md`, `docs`

## Git에서 제외하는 항목

- 각 서비스의 `build/`: Gradle 빌드 결과물
- `vue-frontend/node_modules/`: npm 의존성
- `vue-frontend/dist/`: 프론트 빌드 결과물
- `infra-images.tar`: 제공받은 대용량 Docker 이미지 묶음
- `*.zip`, `.DS_Store`, 로컬 환경 파일

`.gitignore`로 제외된 파일은 소스 저장소에 포함하지 않는다. 현재 프로젝트의 `infra-images.tar`는 약 343MB이므로 Git 저장소가 아닌 Google Drive 등으로 별도 공유한다.

## 조원이 처음 실행할 때

1. Git 저장소를 내려받는다.
2. 별도 공유받은 `infra-images.tar`를 프로젝트 루트에 둔다.
3. 제공 이미지가 필요한 경우 다음 명령으로 이미지를 불러온다.

   ```bash
   docker load -i infra-images.tar
   ```

4. 프론트 의존성을 설치한다.

   ```bash
   cd vue-frontend
   npm ci
   npm run dev
   ```

5. 프로젝트 루트에서 Docker 서비스를 실행한다.

   ```bash
   docker compose up --build
   ```

## 주의

- `node_modules`를 Slack이나 메신저로 공유하지 않는다. macOS에서 `fsevents.node` 보안 경고가 발생할 수 있다.
- `build/`, `dist/`, `node_modules/`는 각자 명령으로 다시 생성한다.
- 공개 저장소에는 비밀번호·토큰을 올리지 않고, 저장소는 우선 비공개로 운영한다.
