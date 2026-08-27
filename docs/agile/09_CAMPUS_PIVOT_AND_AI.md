# GearHub Campus 관리자 수요예측 설계

## 1. AI 문제 정의

GearHub Campus의 AI는 대여자에게 장비를 추천하는 기능이 아니다. 사용자는 대체로 필요한 장비를 이미 알고 있다.

AI가 해결하는 질문은 다음과 같다.

> 각 그룹과 자산 카테고리에서 다음 4주 동안 몇 건의 대여가 발생하고, 현재 재고로 부족하다면 어디에서 이동하거나 무엇을 도입해야 하는가?

사용자는 그룹·학교 관리자이며, 결과는 세 가지 행동으로 이어진다.

1. 현재 재고 유지
2. 다른 그룹의 계획상 여유 재고 이동
3. 이동 후에도 부족한 장비 도입 검토

## 2. 분석 단위와 목표

| 항목 | 정의 |
|---|---|
| 시간 단위 | 월요일 시작 주 |
| 분석 축 | groupId × category |
| 목표값 | 해당 주 REQUESTED 대여 수량 |
| 예측 기간 | 다음 4주 |
| 최소 이력 | 28주 |
| 현재 고정 시드 | 78주, 8개 그룹, 7개 카테고리 |

현재 분석 카테고리는 COMPUTER, DEVICE, CAMERA_AUDIO, PRESENTATION, ELECTRONICS_IOT, MAKER, ACCESSORY다.

## 3. 입력 이벤트

Request는 rental.lifecycle에 다음 이벤트를 발행한다.

- REQUESTED
- APPROVED
- REJECTED
- RETURN_REQUESTED
- RETURNED

Analytics는 eventId를 기본키로 저장한다. 학습에는 REQUESTED 이벤트만 사용하며 quantity를 주 단위로 합산한다. 요청의 시작일과 반납 예정일로 loanDays를 계산하고 값이 없으면 7일을 기본값으로 사용한다.

## 4. 운영 데이터와 시뮬레이션 분리

| 구분 | 저장 | 목적 | 시드 재실행 |
|---|---|---|---|
| 운영 대여 | enrollments | 사용자 화면과 업무 시연 | 데모 표시 행 재생성 |
| 분석 시뮬레이션 | analytics_loan_events, source=SIMULATION | 학습 파이프라인과 평가 | 교체 |
| 신규 운영 이벤트 | analytics_loan_events, source=LIVE | 실제 이력 축적 | 보존 |

운영 화면에는 200개 대여 요청만 둔다. 18개월치 10,814개 분석 이벤트는 별도 테이블에 두어 화면 사용성과 학습량을 동시에 확보한다.

시뮬레이션은 그룹별 규모, 카테고리 선호, 학기 초 기기 수요, 행사철 촬영·발표 수요, 학기 말 제작·컴퓨터 수요, 추세와 잡음을 포함한다. 고정 random seed 42로 같은 결과를 재현한다.

## 5. 전처리와 특징값

### 주간 데이터 구성

1. REQUESTED 이벤트를 groupId, category, weekStart로 집계한다.
2. 관측이 없는 주도 0으로 채워 연속 시계열을 만든다.
3. 그룹·카테고리별 평균 대여기간을 계산한다.
4. 과거 값만 사용하도록 lag와 이동평균을 한 주 뒤로 이동한다.

### 모델 특징

| 유형 | 특징 |
|---|---|
| 범주 | group_id, category |
| 달력 | month, week_of_year |
| 계절 순환 | week_sin, week_cos |
| 과거 수요 | lag_1, lag_2, lag_4 |
| 추세 | rolling_mean_4, rolling_mean_8 |

범주형 값은 OneHotEncoder로 변환한다. Poisson 모델의 수치 특징에는 StandardScaler를 적용하고, 트리 계열은 원래 수치를 사용한다.

## 6. 시간순 학습과 평가

랜덤 분할을 사용하지 않는다. 미래 정보가 과거 학습에 들어가는 누수를 막기 위해 고유 주차를 시간순으로 나눈다.

~~~text
과거 구간: 후보 모델 학습
다음 8주 이내 구간: 후보 선택 검증
마지막 12주 이내 구간: 기준선과 최종 모델 테스트
~~~

데이터 길이에 따라 검증은 전체 주의 약 1/8에서 최소 4주·최대 8주, 테스트는 약 1/5에서 최소 4주·최대 12주를 사용한다.

후보:

- Poisson Regressor
- Random Forest Regressor
- Histogram Gradient Boosting Regressor

기준선:

- 직전 4주 수요의 이동평균

검증 WAPE가 가장 낮은 후보를 선택하고, 학습+검증 구간으로 다시 학습한 뒤 마지막 테스트 구간에서 기준선과 비교한다. 이후 전체 이력으로 최종 모델을 학습해 다음 4주를 재귀 예측한다. 음수 예측은 0으로 자른다.

## 7. 평가 지표

MAE:

    평균 절대 오차

WAPE:

    100 × Σ|실제 - 예측| / Σ|실제|

WAPE는 그룹·카테고리별 수요 규모가 다른 상황에서 전체 오차 비중을 설명하기 쉽다. 수요가 작은 조합에서는 백분율이 크게 보일 수 있으므로 MAE와 함께 제시한다.

개선율:

    100 × (기준선 WAPE - 모델 WAPE) / 기준선 WAPE

## 8. 고정 시드 평가 결과

| 항목 | 결과 |
|---|---:|
| 데이터 기간 | 2025-03-03 ~ 2026-08-24 |
| 이벤트 수 | 10,814 |
| 학습·개발 행 | 3,248 |
| 테스트 행 | 672 |
| 선택 모델 | Histogram Gradient Boosting |
| 기준선 MAE | 1.4717 |
| 모델 MAE | 1.1657 |
| 기준선 WAPE | 70.3414% |
| 모델 WAPE | 55.7159% |
| 기준선 대비 개선 | 20.79% |

이 수치는 동일 seed와 시뮬레이션 데이터의 결과다. 완벽한 정확도를 주장하지 않으며, 실제 운영 효과를 말하려면 LIVE 데이터가 충분히 쌓인 뒤 다시 평가해야 한다.

## 9. 예측을 재고 판단으로 변환

### 필요 수량

각 그룹·카테고리의 4주 예측 중 가장 높은 주간 수요를 사용한다.

    requiredUnits = ceil(peakWeeklyDemand × averageLoanDays / 7 × 1.15)

평균 대여기간으로 한 주의 동시 점유량을 환산하고 15% 운영 여유를 더한다.

### 학교 공용 재고 배분

학교 공용 자산을 모든 그룹이 전량 보유한 것처럼 중복 계산하지 않는다. 같은 카테고리의 그룹별 requiredUnits 비율로 공용 수량을 정수 배분하고, 해당 그룹 필요량을 상한으로 둔다.

### 부족과 위험도

    totalStock = groupOwnedStock + allocatedSharedStock
    shortageUnits = max(0, requiredUnits - totalStock)

| 위험 | 조건 |
|---|---|
| HIGH | shortageUnits가 1 이상 |
| MEDIUM | 부족은 없지만 requiredUnits / totalStock이 0.8 이상 |
| LOW | 그 외 |

availableStock은 현재 대여 중인 수량을 반영한 참고값으로 별도 표시한다.

### 그룹 간 이동 제안

다른 그룹의 같은 카테고리에서 다음 값을 계산한다.

    plannedSurplus = max(0, ownedTotal - requiredUnits)

groupId 순서로 부족 수량을 채울 때까지 이동 제안을 만든다. 현재 구현은 계획상 총수량 여유를 사용하므로 실제 이동 전에 availableStock과 기존 대여 일정을 관리자가 다시 확인해야 한다.

## 10. API와 화면

| API | 결과 |
|---|---|
| POST /api/recommend/analytics/train | 새 모델 실행과 저장 |
| GET /api/recommend/analytics/evaluation | 모델명, 기간, MAE, WAPE, 후보 지표 |
| GET /api/recommend/analytics/forecast?groupId={id} | 7개 카테고리의 4주 예측과 재고 판단 |

관리자 화면은 다음 순서로 보여준다.

1. 데이터 기간과 이벤트 수
2. 기준선과 모델의 MAE·WAPE
3. 선택 모델과 개선율
4. 카테고리별 4주 예측
5. 필요 수량, 총재고, 가용재고, 부족 수량
6. 위험도와 그룹 간 이동 제안

모델 평가를 먼저 보여줘 예측값의 근거를 숨기지 않는다.

## 11. 사용하지 않는 AI 범위

- 외부 생성형 AI API로 설명 문장만 생성
- 대여자 개인 장비 추천
- 개인별 연체 위험 점수
- 이미지 인식 기반 자산 상태 판정

이 기능들은 현재 관리자 재고 의사결정과 직접 연결되지 않거나 데이터·책임 범위가 크므로 MVP에 포함하지 않는다.

## 12. 한계와 다음 실험

- 현재 성능은 합성 이력에 대한 파이프라인 검증이다.
- 외생 변수로 학사 일정, 축제, 행사, 수업 시간표를 사용하지 않는다.
- 그룹 간 이동은 물류 시간과 현재 예약 일정을 고려하지 않는다.
- 신규 그룹·신규 카테고리의 cold start 전략이 없다.
- 자동 재학습, 모델 버전 승격, 드리프트 경보가 없다.

다음 실험은 LIVE 이벤트가 충분해졌을 때 source별 성능을 분리하고, 월별 WAPE와 부족 경보 적중률을 기준으로 모델 유지 여부를 판단하는 것이다.
