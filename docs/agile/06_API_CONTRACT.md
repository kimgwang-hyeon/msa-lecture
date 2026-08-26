# API 계약

## 1. 공통 규칙

- 외부 Base URL: `http://localhost:8080`
- 프론트엔드는 개별 포트 `8081~8085`가 아니라 API Gateway를 호출한다.
- 인증 API를 제외한 요청은 `Authorization: Bearer <access_token>`을 사용한다.
- Gateway가 검증한 사용자 정보를 `X-User-Id`, `X-User-Email`, `X-User-Role` 헤더로 하위 서비스에 전달한다.
- 기존 템플릿 호환을 위해 URL의 `courses`, `enrollments`, `payments`, `recommend` 명칭은 유지한다.

Spring 서비스의 일반 응답 형식은 다음과 같다.

```json
{
  "success": true,
  "message": "성공",
  "data": {}
}
```

## 2. Gateway 라우팅

| 외부 경로 | 논리 대상 서비스 | 포트 |
|---|---|---:|
| `/api/users/**` | Member Service | 8081 |
| `/api/courses/**` | Asset Service | 8082 |
| `/api/enrollments/**` | Request Service | 8083 |
| `/api/payments/**` | Budget Service | 8084 |
| `/api/recommend/**` | Alternative Service | 8085 |
| `/oauth2/**`, `/login/**` | Auth Server | 9000 |

## 3. Member Service

| Method | Endpoint | 사용자 | 설명 |
|---|---|---|---|
| POST | `/api/users/register` | 공개 | 교육생·운영진 회원가입 |
| GET | `/api/users/me` | 로그인 사용자 | 현재 사용자 정보 |
| GET | `/api/users/{id}` | 로그인 사용자 | 사용자 단건 조회 |
| GET | `/api/users/internal/{id}` | 내부 호출 | 사용자 정보 조회 |

### 회원가입 요청

```json
{
  "email": "student@example.com",
  "password": "password123",
  "name": "홍길동",
  "role": "STUDENT"
}
```

`role`은 `STUDENT`(교육생) 또는 `INSTRUCTOR`(운영진)다.

## 4. Asset Service

| Method | Endpoint | 사용자 | 설명 |
|---|---|---|---|
| GET | `/api/courses` | 로그인 사용자 | 대여 가능한 보유 교보재 목록 |
| GET | `/api/courses/{id}` | 로그인 사용자 | 교보재 상세·재고 |
| GET | `/api/courses/category/{category}` | 로그인 사용자 | 카테고리별 목록 |
| POST | `/api/courses` | 운영진 | 보유 교보재 등록 |
| GET | `/api/courses/internal/exists/{id}` | Request | 존재 여부 |
| GET | `/api/courses/internal/{id}` | Request | 교보재 내부 상세 |
| POST | `/api/courses/internal/{id}/borrow` | Request | 승인 시 재고 차감 |
| GET | `/api/courses/internal/recommend` | Alternative | 카테고리·제외 ID 기반 목록 |

현재 프론트 API 모듈에 `PUT /api/courses/{id}` 함수가 있지만 백엔드 엔드포인트는 없다. MVP 화면에서 사용하지 않으며 API 계약에 포함하지 않는다.

### 보유 교보재 등록 요청

```json
{
  "title": "iPhone 15 Pro",
  "description": "iOS 실기기 테스트용",
  "category": "DEVICE",
  "price": 1500000,
  "itemType": "OWNED",
  "totalQuantity": 2
}
```

### 교보재 응답 예시

```json
{
  "id": 1,
  "title": "iPhone 15 Pro",
  "description": "iOS 실기기 테스트용",
  "category": "DEVICE",
  "price": 1500000,
  "itemType": "OWNED",
  "totalQuantity": 2,
  "availableQuantity": 1,
  "purchaseUrl": null,
  "instructorId": 2,
  "enrollmentCount": 1,
  "status": "ACTIVE"
}
```

카테고리 값: `DEVICE`, `COMPUTER`, `SERVER_CLOUD`, `ELECTRONICS_IOT`, `MAKER`, `CAMERA_AUDIO`, `ETC`와 기존 템플릿 카테고리.

## 5. Request Service

| Method | Endpoint | 사용자 | 설명 |
|---|---|---|---|
| POST | `/api/enrollments` | 교육생 | 보유 교보재 대여 신청 |
| POST | `/api/enrollments/purchases` | 교육생 | 신규 교보재 구매요청 |
| GET | `/api/enrollments/my` | 교육생 | 내 대여·구매 신청 |
| GET | `/api/enrollments/pending?requestType=LOAN` | 운영진 | 대여 승인 대기 목록 |
| POST | `/api/enrollments/{id}/approve` | 운영진 | 대여 승인 |
| POST | `/api/enrollments/{id}/reject` | 운영진 | 대여 반려 |
| GET | `/api/enrollments/internal/history/{userId}` | Alternative | 활성 대여 이력 |

### 대여 신청 요청

```json
{
  "courseId": 1,
  "reason": "iOS 팀 프로젝트에서 실제 기기 테스트가 필요합니다."
}
```

### 대여 신청 응답 핵심

```json
{
  "id": 1,
  "userId": 1,
  "courseId": 1,
  "requestType": "LOAN",
  "reason": "iOS 팀 프로젝트에서 실제 기기 테스트가 필요합니다.",
  "reviewComment": null,
  "status": "PENDING",
  "course": {
    "title": "iPhone 15 Pro",
    "category": "스마트기기",
    "price": 1500000,
    "itemType": "OWNED",
    "totalQuantity": 2,
    "availableQuantity": 2
  }
}
```

### 대여 반려 요청

```json
{
  "reviewComment": "동일 기간에 운영 수업 우선 배정이 필요합니다."
}
```

### 신규 구매요청

```json
{
  "title": "NVIDIA Jetson Orin Nano 개발자 키트",
  "description": "컴퓨터 비전과 엣지 AI 실습 장비",
  "category": "ELECTRONICS_IOT",
  "unitPrice": 499000,
  "quantity": 2,
  "purchaseUrl": "https://example.com/jetson-orin-nano",
  "reason": "실시간 영상 추론 프로젝트를 실제 장비에서 검증하려고 합니다.",
  "alternativeChecked": true
}
```

검증 규칙:

- `unitPrice > 0`
- `quantity > 0`
- `purchaseUrl`은 `http://` 또는 `https://`로 시작
- `reason` 필수
- `alternativeChecked == true`
- 총액은 Request Service가 `unitPrice × quantity`로 계산

## 6. Budget Service

| Method | Endpoint | 사용자 | 설명 |
|---|---|---|---|
| POST | `/api/payments/internal/request` | Request | PENDING 예산 요청 생성 |
| GET | `/api/payments?status=PENDING` | 운영진 | 예산 검토 대기 목록 |
| POST | `/api/payments/{id}/approve` | 운영진 | 예산 승인·승인번호 생성 |
| POST | `/api/payments/{id}/reject` | 운영진 | 예산 반려 |
| GET | `/api/payments/{id}` | 로그인 사용자 | 예산 단건 조회 |
| GET | `/api/payments/user/{userId}` | 로그인 사용자 | 사용자별 예산 내역 |

### 내부 예산 요청

```json
{
  "userId": 3,
  "courseId": 9,
  "amount": 998000
}
```

### 예산 승인 응답

```json
{
  "paymentId": 1,
  "userId": 3,
  "courseId": 9,
  "amount": 998000,
  "status": "COMPLETED",
  "transactionId": "BUDGET-00A37A60"
}
```

## 7. Alternative Service

| Method | Endpoint | 사용자 | 설명 |
|---|---|---|---|
| GET | `/api/recommend/alternatives?category={category}` | 교육생 | 구매 전 같은 카테고리의 가용 대체재 |
| GET | `/api/recommend/{userId}` | 로그인 사용자 | 기존 개인화 추천 흐름 |
| GET | `/health` | 운영 확인 | FastAPI 상태 확인 |

### 대체재 응답

```json
{
  "category": "ELECTRONICS_IOT",
  "alternatives": [
    {
      "id": 4,
      "title": "Raspberry Pi 5 IoT Kit",
      "category": "ELECTRONICS_IOT",
      "itemType": "OWNED",
      "totalQuantity": 4,
      "availableQuantity": 4,
      "price": 180000,
      "instructorId": 2,
      "enrollmentCount": 0,
      "status": "ACTIVE"
    }
  ],
  "message": "대여 가능한 보유 교보재를 먼저 확인해 주세요"
}
```

## 8. 상태 계약

| 도메인 | 상태 | 의미 |
|---|---|---|
| Request | `PENDING` | 운영진 검토 전 |
| Request | `ACTIVE` | 대여 또는 구매 승인 |
| Request | `REJECTED` | 대여 또는 예산 반려 |
| Request | `CANCELLED` | 취소(현재 UI 미구현) |
| Budget | `PENDING` | 예산 검토 전 |
| Budget | `COMPLETED` | 예산 승인 |
| Budget | `FAILED` | 예산 반려 |
| Budget | `CANCELLED` | 취소(현재 UI 미구현) |

## 9. 대표 오류 계약

| HTTP | 상황 | 프론트 처리 |
|---:|---|---|
| 400 | 필수값·URL·금액·대체재 확인 검증 실패 | 필드 오류 메시지 표시 |
| 401 | Access Token 없음·만료 | 로그인 화면으로 이동 |
| 403 | 역할·Scope 부족 또는 Alternative 직접 호출 | 권한 안내, Gateway 주소 확인 |
| 404 | 교보재·신청·예산 ID 없음 | 목록으로 이동 후 새로고침 |
| 409 | 재고 부족, 이미 처리된 요청 등 상태 충돌 | 최신 상태를 다시 조회하고 안내 |

## 10. 권한 관련 현재 제한

프론트 라우터는 `STUDENT`와 `INSTRUCTOR` 화면을 구분한다. 다만 운영진 승인 API의 서버 측 역할 강제는 보강 대상 `S-01`이다. 데모에서는 역할이 올바른 계정을 사용하고, 실서비스 전에는 메서드 보안을 추가해야 한다.
