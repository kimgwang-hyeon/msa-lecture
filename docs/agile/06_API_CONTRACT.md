# GearHub Campus API 계약

## 1. 공통 규칙

외부 기준 주소:

    http://localhost:8080

브라우저는 Authorization: Bearer {accessToken}을 Gateway에 보낸다. Gateway가 인증 정보에서 X-User-Id와 X-User-Role을 만들어 하위 서비스에 전달한다. X-User-Id를 브라우저가 임의로 신뢰시키는 구조가 아니다.

Spring 서비스의 일반 응답:

~~~json
{
  "success": true,
  "message": "성공",
  "data": {}
}
~~~

FastAPI Analytics 응답은 위 래퍼 없이 response model을 직접 반환한다.

오류 상태:

| 상태 | 의미 |
|---:|---|
| 400 | 유효하지 않은 값, 존재하지 않는 리소스 |
| 401 | 토큰 없음 또는 검증 실패 |
| 403 | 요청된 scope 또는 권한 부족 |
| 409 | 재고 부족, 잘못된 상태 전이, 권한 충돌 |
| 503 | 하위 서비스 또는 분석 실행 실패 |

물리 필드 courseId는 제품에서 assetId, enrollment id는 requestId, payment는 budget review를 뜻한다.

## 2. Gateway 라우팅

| Prefix | 대상 |
|---|---|
| /oauth2, /login, /userinfo | Auth |
| /api/users | Member |
| /api/courses | Asset |
| /api/enrollments | Request |
| /api/payments | Budget |
| /api/recommend | Demand Analytics 및 호환 API |

## 3. Member API

### 사용자

| Method | Path | 용도 |
|---|---|---|
| POST | /api/users/register | 사용자 등록 |
| GET | /api/users/me | 현재 사용자 조회 |
| GET | /api/users/{id} | 사용자 조회 |
| GET | /api/users/internal/{id} | 서비스 내부 사용자 조회 |

등록 요청:

~~~json
{
  "email": "member@example.edu",
  "password": "password",
  "name": "홍길동",
  "role": "STUDENT"
}
~~~

조직 역할은 STUDENT, INSTRUCTOR다.

### 그룹

| Method | Path | 용도 | 권한 |
|---|---|---|---|
| POST | /api/users/groups | 그룹 생성 | 로그인 사용자 |
| GET | /api/users/groups/my | 내 그룹 목록 | 로그인 사용자 |
| POST | /api/users/groups/join | 초대코드 가입 | 로그인 사용자 |
| GET | /api/users/groups/{groupId} | 그룹 상세 | 그룹 멤버 또는 학교 관리자 |
| GET | /api/users/groups/{groupId}/members | 구성원 목록 | 그룹 관리자 |
| PATCH | /api/users/groups/{groupId}/members/{memberId}/role | 역할 변경 | 그룹 관리자 |
| GET | /api/users/groups/internal/{groupId}/access/{userId} | 접근 판정 | 내부 서비스 |

그룹 생성:

~~~json
{
  "name": "컴퓨터공학과",
  "description": "컴퓨터공학과 공용 장비"
}
~~~

초대 가입:

~~~json
{
  "inviteCode": "AB12CD34"
}
~~~

역할 변경:

~~~json
{
  "role": "MANAGER"
}
~~~

GroupResponse의 inviteCode는 currentRole이 MANAGER일 때만 값이 있고 일반 구성원에게는 null이다.

## 4. Asset API

| Method | Path | 용도 |
|---|---|---|
| POST | /api/courses | 자산 등록 |
| GET | /api/courses?groupId={id} | 접근 가능한 자산 목록 |
| GET | /api/courses/{id} | 자산 상세 |
| GET | /api/courses/category/{category}?groupId={id} | 카테고리 조회 |
| GET | /api/courses/internal/exists/{id} | 자산 존재 확인 |
| GET | /api/courses/internal/{id} | 내부 상세 조회 |
| GET | /api/courses/internal/analytics/assets | 분석용 현재 재고 |
| POST | /api/courses/internal/acquisition-requests | 도입 대상 생성 |
| POST | /api/courses/internal/{id}/enrollment-count | 호환용 누적 대여 횟수 증가 |
| POST | /api/courses/internal/{id}/borrow | 재고 1 차감 |
| POST | /api/courses/internal/{id}/return | 재고 1 복구 |
| POST | /api/courses/internal/{id}/receive | 입고·자산 전환 |

자산 등록 요청:

~~~json
{
  "title": "MacBook Pro 14",
  "description": "AI 실습용 노트북",
  "category": "COMPUTER",
  "price": 2890000,
  "itemType": "OWNED",
  "totalQuantity": 5,
  "ownerGroupId": 1,
  "visibility": "GROUP",
  "pickupLocation": "공학관 101호",
  "maxLoanDays": 7
}
~~~

자산 응답 핵심:

~~~json
{
  "id": 8,
  "title": "MacBook Pro 14",
  "category": "COMPUTER",
  "itemType": "OWNED",
  "totalQuantity": 5,
  "availableQuantity": 3,
  "ownerGroupId": 1,
  "visibility": "GROUP",
  "pickupLocation": "공학관 101호",
  "maxLoanDays": 7,
  "status": "ACTIVE"
}
~~~

현재 UI와 분석이 사용하는 카테고리는 COMPUTER, DEVICE, CAMERA_AUDIO, PRESENTATION, ELECTRONICS_IOT, MAKER, ACCESSORY다. 엔티티는 템플릿 호환을 위해 BACKEND, FRONTEND, DEVOPS, DATA_SCIENCE, MOBILE, SECURITY, DATABASE, SERVER_CLOUD, OTHER, ETC도 허용한다.

## 5. Request API

| Method | Path | 용도 | 권한 |
|---|---|---|---|
| POST | /api/enrollments | 대여 신청 | 그룹 멤버 |
| POST | /api/enrollments/acquisitions | 도입 요청 | 그룹 멤버 |
| POST | /api/enrollments/purchases | 도입 요청 별칭 | 그룹 멤버 |
| GET | /api/enrollments/my?groupId={id} | 내 요청 | 본인 |
| GET | /api/enrollments/group/{groupId}?requestType={type}&status={status} | 그룹 요청 조회 | 그룹 관리자 |
| GET | /api/enrollments/pending?requestType={type}&groupId={id} | 대기 요청 조회 | 그룹 관리자 |
| POST | /api/enrollments/{id}/approve | 대여 승인 | 그룹 관리자 |
| POST | /api/enrollments/{id}/group-approve | 도입 1차 승인 | 그룹 관리자 |
| POST | /api/enrollments/{id}/reject | 요청 반려 | 그룹 관리자 |
| POST | /api/enrollments/{id}/return-request | 반납 요청 | 요청자 본인 |
| POST | /api/enrollments/{id}/return-confirm | 반납 확인 | 그룹 관리자 |
| POST | /api/enrollments/{id}/receive | 도입 입고 | 그룹 관리자 |
| GET | /api/enrollments/user/{userId}?groupId={id} | 사용자 요청 조회 | 호환 API |
| GET | /api/enrollments/internal/history/{userId} | 활성 자산 ID 조회 | 내부 호환 API |

대여 신청:

~~~json
{
  "courseId": 8,
  "groupId": 1,
  "reason": "캡스톤 프로젝트 개발",
  "requestedFrom": "2026-08-28",
  "dueDate": "2026-09-03"
}
~~~

검증 규칙:

- 요청자는 groupId의 활성 멤버여야 한다.
- 자산은 OWNED와 ACTIVE여야 한다.
- GROUP 자산의 ownerGroupId는 요청 groupId와 같아야 한다.
- availableQuantity는 1 이상이어야 한다.
- dueDate는 requestedFrom보다 빠를 수 없다.
- 포함 일수는 maxLoanDays를 넘을 수 없다.
- 같은 사용자가 같은 자산을 PENDING, ACTIVE, RETURN_REQUESTED로 중복 보유할 수 없다.

도입 요청:

~~~json
{
  "title": "열화상 카메라",
  "description": "전자회로 발열 분석",
  "groupId": 1,
  "category": "CAMERA_AUDIO",
  "unitPrice": 1850000,
  "quantity": 1,
  "purchaseUrl": "https://example.com/thermal-camera",
  "reason": "캡스톤 실험 장비 부족",
  "alternativeChecked": true
}
~~~

alternativeChecked는 입력 호환 필드다. 현재 제품의 AI 판단이나 승인 필수 조건으로 사용하지 않는다.

반려:

~~~json
{
  "reviewComment": "현재 그룹 보유 장비로 대체 가능합니다."
}
~~~

입고:

~~~json
{
  "receivedQuantity": 1,
  "pickupLocation": "공학관 장비실",
  "visibility": "GROUP"
}
~~~

Request 응답은 id, userId, courseId, groupId, requestType, reason, reviewComment, status, requestedFrom, dueDate, approvedAt, returnedAt, reviewedBy, overdue, course 요약을 포함한다.

## 6. Budget API

| Method | Path | 용도 | 권한 |
|---|---|---|---|
| POST | /api/payments/internal/request | 예산 요청 생성 | Request 내부 호출 |
| GET | /api/payments?status={status}&groupId={id} | 예산 검토 목록 | 학교 관리자 |
| POST | /api/payments/{id}/approve | 예산 승인 | 학교 관리자 |
| POST | /api/payments/{id}/reject | 예산 반려 | 학교 관리자 |
| GET | /api/payments/{id} | 예산 단건 조회 | 현재 호환 조회 |
| GET | /api/payments/user/{userId} | 사용자별 예산 조회 | 현재 호환 조회 |

내부 예산 요청:

~~~json
{
  "userId": 2,
  "courseId": 126,
  "requestId": 409,
  "groupId": 1,
  "amount": 3700000
}
~~~

Budget 상태:

- PENDING: 학교 검토 대기
- COMPLETED: 승인, 예산 승인번호 생성
- FAILED: 반려
- CANCELLED: 취소

approve 또는 reject 뒤 payment.completed가 발행되고 Request가 BUDGET_APPROVED 또는 REJECTED로 전이한다.

## 7. Demand Analytics API

모든 Analytics API는 유효한 Bearer 토큰을 요구한다. 관리자 전용 화면은 프론트 라우팅에서 제한하지만, 현재 Analytics API 자체는 관리자 역할을 별도로 검사하지 않는다.

| Method | Path | 용도 |
|---|---|---|
| POST | /api/recommend/analytics/train | 현재 이벤트로 모델 재학습 |
| GET | /api/recommend/analytics/evaluation | 최신 평가 결과 |
| GET | /api/recommend/analytics/forecast?groupId={id} | 그룹의 다음 4주 예측 |

평가 응답:

~~~json
{
  "runId": 1,
  "trainedAt": "2026-08-27T12:00:00",
  "modelName": "hist_gradient_boosting",
  "baselineMae": 1.4717,
  "modelMae": 1.1657,
  "baselineWape": 70.3414,
  "modelWape": 55.7159,
  "improvementPercent": 20.79,
  "trainRows": 3248,
  "testRows": 672,
  "eventCount": 10814,
  "dataStart": "2025-03-03",
  "dataEnd": "2026-08-24",
  "candidateMetrics": {}
}
~~~

예측 항목:

~~~json
{
  "groupId": 1,
  "category": "COMPUTER",
  "forecastDemand": 12.4,
  "averageLoanDays": 6.8,
  "requiredUnits": 4,
  "totalStock": 6,
  "availableStock": 2,
  "sharedStock": 1,
  "shortageUnits": 1,
  "riskLevel": "HIGH",
  "weekly": [
    {
      "weekStart": "2026-08-31",
      "predictedDemand": 3.1
    }
  ],
  "transferSuggestions": [
    {
      "fromGroupId": 3,
      "category": "COMPUTER",
      "suggestedQuantity": 1
    }
  ]
}
~~~

## 8. 상태 계약

### Request

| 타입 | 허용 흐름 |
|---|---|
| LOAN | PENDING → ACTIVE → RETURN_REQUESTED → RETURNED |
| LOAN 반려 | PENDING → REJECTED |
| PURCHASE | PENDING → GROUP_APPROVED → BUDGET_APPROVED → RECEIVED |
| PURCHASE 반려 | PENDING 또는 GROUP_APPROVED → REJECTED |

CANCELLED는 엔티티에 정의되어 있으나 현재 사용자 화면의 핵심 흐름에는 포함하지 않는다.

### Asset

| 필드 | 값 |
|---|---|
| itemType | OWNED, PURCHASE_REQUEST |
| visibility | ORGANIZATION, GROUP |
| status | ACTIVE, INACTIVE |

## 9. 호환 API

다음 계약은 템플릿 실행 호환을 위해 남아 있으나 GearHub Campus의 주 데모 경로가 아니다.

- GET /api/courses/internal/recommend
- GET /api/recommend/alternatives
- GET /api/recommend/health
- GET /api/recommend/{userId}
- GET /api/enrollments/internal/history/{userId}
- POST /api/enrollments/purchases

새 기능은 그룹 중심 API와 /api/recommend/analytics를 사용한다.

## 10. 현재 보안 한계

- 일부 내부·호환 조회는 서비스 포트를 직접 호출할 때 제품 수준 인증이 충분하지 않다.
- Budget의 역할 헤더 검증은 Gateway가 헤더를 제공한다는 전제다.
- Analytics API는 현재 토큰 유효성만 확인하므로 서버 측 관리자 역할 검증을 추가해야 한다.
- 브라우저 OAuth는 제공 Auth Server 호환을 우선해 운영 제품의 PKCE 또는 BFF 전환이 필요하다.
- 서비스 간 mTLS, 별도 service token, 세밀한 감사로그는 후속 범위다.
