# SKALA GearHub

SKALA GearHub는 교육기관·기업이 내부 장비의 보유·대여·구매요청·예산 검토를 한 흐름으로 관리하는 B2B SaaS의 애자일 MSA 실습 버전입니다. 현재 구현은 SKALA 한 조직을 위한 단일 테넌트 MVP입니다.

## 문제와 사용자

- 고객 조직: 교육기관, 기업 교육팀·IT·총무·R&D 조직입니다.
- 구성원: 어떤 장비를 보유하고 있는지 확인하기 어렵고, 필요한 새 장비를 일관된 방식으로 요청하기 어렵습니다.
- 자산 운영자·예산 승인자: 대여 가능 수량, 신청 사유, 구매 링크와 예상 금액을 한곳에서 검토할 필요가 있습니다.
- 서비스 목표: 중복 구매와 승인 리드타임을 줄이고 자산 활용과 요청 상태를 투명하게 관리합니다.

완성형 SaaS에 필요한 멀티테넌시, 고객사 SSO, 조직별 결재 정책, 감사로그와 요금제는 이번 이틀 MVP에 포함하지 않고 제품화 백로그로 관리합니다.

## GearHub 서비스 구성

| GearHub 서비스명 | 핵심 책임 | 소유 데이터(물리 테이블) | 기존 코드 기반 |
|---|---|---|---|
| Member Service (`member-service`) | 교육생·운영진 계정과 역할 | `users` | User Service |
| Asset Service (`asset-service`) | 보유 교보재, 자산가치와 가용 재고 | `courses` | Course Service |
| Request Service (`request-service`) | 대여·신규 구매 신청과 상태 | `enrollments` | Enrollment Service |
| Budget Service (`budget-service`) | 구매요청 금액과 예산 승인·반려 | `payments` | Payment Service |
| Alternative Service (`alternative-service`) | 구매 전 대여 가능한 대체재 추천 | 자체 테이블 없음 | Recommend Service |

Auth Server, API Gateway, Eureka, Kafka, MariaDB는 GearHub의 공통 인프라 이름을 그대로 사용합니다.

### ERD 작성 기준

- `users` 1:N `courses`: 운영진이 교보재를 등록합니다.
- `users` 1:N `enrollments`: 교육생이 신청을 생성합니다.
- `courses` 1:N `enrollments`: 하나의 교보재에 여러 신청이 연결됩니다.
- `users` 1:N `payments`: 교육생별 구매 예산 요청을 기록합니다.
- `courses` 1:N `payments`: 구매요청 상품별 예산 검토를 기록합니다.
- Alternative Service는 API로 필요한 데이터를 조회하므로 ERD에 별도 테이블을 만들지 않습니다.

## 기존 코드 재사용

이번 실습은 MSA 구조를 재설계하지 않고 기존 코드를 최소한으로 확장했습니다.

| 기존 코드 | GearHub에서의 의미 |
|---|---|
| User / STUDENT | 교육생 |
| User / INSTRUCTOR | 운영진 |
| Course | 보유 교보재 또는 구매요청 상품 |
| Enrollment | 대여 신청 또는 구매 신청 |
| Payment | 실제 결제가 아닌 예산 검토 |
| Recommend | 구매 전 보유 대체재 추천 |

업무 서비스의 논리 이름과 Eureka 등록명은 GearHub에 맞게 변경했습니다. 실습 코드의 변경량을 줄이기 위해 포트, API 경로, Kafka 토픽, 물리 테이블, Java 패키지와 소스 디렉터리는 기존 이름을 유지합니다. 사전 제공 API Gateway 이미지는 변경하지 않고 Compose 환경변수로 새 Eureka 서비스명을 연결합니다.

## 핵심 흐름

### 보유 교보재 대여

1. 교육생이 보유 교보재와 가용 수량을 조회합니다.
2. 사용 목적을 입력해 대여를 신청합니다.
3. 운영진이 신청을 승인하거나 반려합니다.
4. 승인 시 `availableQuantity`가 1 감소하고 신청 상태가 `ACTIVE`가 됩니다.

### 신규 교보재 구매요청

1. 교육생이 상품명, 카테고리, 단가, 수량, 구매 링크와 사유를 입력합니다.
2. Alternative Service가 동일 카테고리의 대여 가능한 보유 교보재를 먼저 보여줍니다.
3. 대체재 확인 후 구매요청을 제출합니다.
4. Budget Service가 요청 총액을 `PENDING` 상태로 저장합니다.
5. 운영진의 승인·반려 결과가 `payment.completed` 이벤트로 전달됩니다.
6. Request Service가 구매요청 상태를 `ACTIVE` 또는 `REJECTED`로 변경합니다.

## 주요 API

- `GET /api/courses`: 보유 교보재 목록
- `POST /api/courses`: 운영진 교보재 등록
- `POST /api/enrollments`: 교보재 대여 신청
- `POST /api/enrollments/purchases`: 신규 교보재 구매요청
- `GET /api/enrollments/my`: 내 신청 목록
- `GET /api/enrollments/pending?requestType=LOAN`: 운영진 대여 승인 대기 목록
- `POST /api/enrollments/{id}/approve`: 대여 승인
- `POST /api/enrollments/{id}/reject`: 대여 반려
- `GET /api/recommend/alternatives?category=DEVICE`: 보유 대체재 추천
- `GET /api/payments?status=PENDING`: 예산 검토 대기 목록
- `POST /api/payments/{id}/approve`: 예산 승인
- `POST /api/payments/{id}/reject`: 예산 반려

## 상태와 금액

- Request: `PENDING`(검토 중), `ACTIVE`(승인), `REJECTED`(반려)
- Budget: `PENDING`(예산 검토), `COMPLETED`(승인), `FAILED`(반려)
- 보유 교보재 가격은 학생에게 청구하는 금액이 아니라 자산가치입니다.
- 구매요청 총액은 서버에서 `단가 × 수량`으로 다시 계산합니다.

## 최종 재배포 시 실행

현재는 사용자의 요청에 따라 Docker 이미지를 다시 만들지 않았다. 아래 명령은 새 GearHub 서비스명을 실제 컨테이너와 Eureka에 적용하기로 팀이 결정한 뒤 사용하는 참고 절차다.

```bash
docker load -i infra-images.tar
docker compose build
docker compose up -d

cd vue-frontend
npm install
npm run dev
```

- 프론트엔드: `http://localhost:3000`
- API Gateway: `http://localhost:8080`
- Eureka: `http://localhost:8761`

기존 MariaDB 볼륨이 있다면 JPA `ddl-auto: update`가 추가 컬럼을 생성합니다. 완전히 새로운 데모 데이터로 시작할 때만 볼륨을 별도로 초기화합니다.

## 이번 MVP에서 제외한 기능

- 실제 결제와 PG 연동
- 실제 발주, 배송, 입고 자동화
- 반납과 재고 복구
- 날짜별 예약 충돌 계산
- 알림, 파일 업로드, 복잡한 추천 모델
- 포트·API 경로·물리 테이블의 전면 개명
- 사전 제공 Gateway·Auth·Eureka 이미지 수정
