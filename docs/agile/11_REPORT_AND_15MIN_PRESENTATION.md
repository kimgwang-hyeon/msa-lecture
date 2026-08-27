# GearHub Campus 최종 보고서·15분 발표 설계안

기준일: 2026-08-27

## 0. 문서 목적

이 문서는 최종 보고서와 발표 PPT를 같은 논리로 만들기 위한 단일 뼈대다. PDF 가이드의 제출 흐름과 현재 구현된 GearHub Campus의 제품·Sprint·MSA·AI·검증 결과를 연결한다.

발표 전체를 관통할 한 문장은 다음과 같다.

> GearHub Campus는 한 대학교의 학과·연구실·동아리가 흩어진 장비를 함께 대여·반납·도입하고, 관리자가 자체 대여 이력으로 다음 4주 부족 재고를 예측해 이동 또는 도입을 결정하도록 돕는 B2B 자산 운영 플랫폼이다.

발표 순서는 반드시 다음 인과관계를 유지한다.

~~~text
왜 필요한가
이해관계자와 Pain Point
        ↓
무엇을 해결하는가
제품 흐름과 AI의 역할
        ↓
어떻게 나눠 만들었는가
Product Backlog와 Sprint 1·2
        ↓
어떻게 구현했는가
MSA, API, Kafka, 데이터
        ↓
실제로 무엇이 달라졌는가
화면, 상태 변화, 모델 평가, 배운 점
~~~

## 1. PDF 가이드 요구와 우리 프로젝트의 대응

### 1.1 발표 기획서의 필수 6단계

| PDF 가이드 요구 | 평가자가 확인할 질문 | GearHub Campus에서 보여줄 근거 |
|---|---|---|
| 이해관계자 가치와 Pain Point | 누구의 어떤 불편을 해결하는가 | 구성원·그룹 관리자·학교 관리자의 문제 |
| AI 솔루션 | AI가 문제 해결에서 무슨 역할을 하는가 | 4주 수요예측, 부족 수량, 이동 제안 |
| Sprint 구분 | 왜 이것이 Sprint 1이고 나머지가 Sprint 2인가 | 핵심 거래 흐름과 확장·자동화의 분리 |
| 아키텍처 구성도 | 서비스 책임과 통신이 어떻게 나뉘는가 | Member·Asset·Request·Budget·Analytics |
| API 명세 | 화면이 실제 백엔드와 연결되는가 | 현재 Gateway 엔드포인트와 상태 계약 |
| 동작 화면 스냅샷 | 요청 전후 상태가 실제로 달라지는가 | 재고 감소·복구, 예산 상태, AI 대시보드 |

### 1.2 Sprint 기준

PDF의 Sprint 기준을 현재 제품에 적용하면 다음과 같다.

| 기준 | PDF의 의미 | GearHub Campus 적용 |
|---|---|---|
| Sprint 1 | 2~3개 도메인 서비스 안에서 사용자가 처음부터 끝까지 쓸 수 있는 Walking Skeleton | Member·Asset·Request로 그룹 자산 대여와 반납 완료 |
| Sprint 2 | Sprint 1에서 다루지 않은 결제·Kafka·추천 영역 확장 | Budget·Kafka·Demand Analytics로 도입·예산·수요예측 완성 |
| Review | 동작하는 결과를 직접 시연 | 상태와 재고의 전후 변화를 실제 화면으로 확인 |
| Retrospective | 계획과 실행의 Gap을 정리하고 다음 우선순위 조정 | 개인 추천 대신 관리자 수요예측을 선택한 이유 설명 |

Sprint를 단순히 날짜나 서비스 개수로 나눈 것이 아니다. “이 기능이 없으면 사용자가 핵심 가치를 얻을 수 있는가?”를 기준으로 먼저 완전한 거래 흐름을 만들고, 다음 Sprint에서 자동화와 데이터 기반 의사결정을 붙였다.

## 2. 보고서 전체 목차

최종 보고서는 다음 9개 장으로 작성하면 된다. 이 문서의 뒤쪽 내용은 각 장에 그대로 옮겨 사용할 수 있다.

| 장 | 제목 | 반드시 답할 질문 | 핵심 증거 |
|---:|---|---|---|
| 1 | 프로젝트 개요 | 무엇을 만들었고 누구를 위한 것인가 | 한 문장 제품 정의 |
| 2 | 이해관계자와 문제 정의 | 왜 이 주제를 선택했는가 | Stakeholder·Pain Point 표 |
| 3 | 솔루션과 도메인 설계 | 어떤 업무 흐름으로 해결하는가 | 대여·반납·도입 흐름 |
| 4 | Agile 기획 | Backlog와 우선순위를 어떻게 만들었는가 | Epic, User Story, MoSCoW, AC |
| 5 | Sprint 1·2 실행 | 왜 두 Increment로 나눴는가 | Sprint Goal, Backlog, Review |
| 6 | MSA·API 설계 | 서비스 책임과 통신은 어떻게 나뉘는가 | 구성도, API, Kafka |
| 7 | AI 수요예측 | AI가 왜 필요하고 어떻게 검증했는가 | 데이터, 기준선, 모델, WAPE |
| 8 | 구현·검증·시연 | 실제로 동작한다는 근거는 무엇인가 | Docker, 테스트, 전후 화면 |
| 9 | 회고와 확장 계획 | 무엇을 배웠고 무엇이 남았는가 | Keep·Problem·Try, 한계 |

## 3. 보고서용 Executive Summary 초안

GearHub Campus는 한 대학교 안의 학과·연구실·동아리가 학교 공용 장비와 그룹 전용 장비를 함께 운영하는 B2B 자산 관리 플랫폼이다. 구성원은 자신이 속한 그룹에서 장비를 조회하고 기간과 목적을 입력해 대여·반납·도입을 요청하며, 그룹 관리자는 대여 승인과 실물 반납을 확인하고 학교 관리자는 예산을 검토한다. 핵심 거래 흐름은 Sprint 1에서 그룹·자산·대여·반납의 Walking Skeleton으로 완성했고, Sprint 2에서 도입·예산·Kafka 이벤트와 관리자용 수요예측으로 확장했다. AI는 외부 생성형 API가 아니라 78주 대여 이력을 scikit-learn으로 분석해 그룹·카테고리별 다음 4주 수요와 부족 수량을 계산한다. 고정 시드 평가에서 Histogram Gradient Boosting은 최근 4주 평균 기준선보다 WAPE를 20.79% 개선했으며, 예측 결과를 다른 그룹의 재고 이동 또는 신규 도입 검토로 연결했다.

## 4. 왜 이 주제를 선택했는가

### 4.1 B2B 제품 범위

고객은 개인 대여자가 아니라 대학이라는 조직이다. 학과·연구실·동아리는 대학 내부의 운영 그룹이며, 제품은 이들이 보유한 자산과 승인 책임을 하나의 흐름으로 연결한다.

현재 범위:

- 한 대학교
- 여러 학과·연구실·동아리
- 학교 공용 자산과 그룹 전용 자산
- 단일 애플리케이션의 동적 그룹 워크스페이스

현재 범위가 아닌 것:

- 여러 대학을 완전히 격리하는 멀티테넌트 SaaS
- 개인 간 중고 물품 대여 플랫폼
- 일반 소비자 대상 B2C 쇼핑몰

### 4.2 이해관계자

| 이해관계자 | 하려는 일 | 현재 Pain Point | 제공 가치 |
|---|---|---|---|
| 일반 구성원 | 필요한 장비를 빌리고 반납 | 사용 가능한 장비·수량·승인 상태가 흩어짐 | 한 그룹 화면에서 조회·신청·상태 확인 |
| 그룹 관리자 | 그룹 자산과 신청을 운영 | 신청·실물 반납·재고 복구가 수기로 분리됨 | 승인과 반납 확인을 재고 변화에 연결 |
| 학교 관리자 | 전체 예산과 공용 자산을 운영 | 부족이 발생한 뒤 구매하고 그룹 간 여유를 보기 어려움 | 예산 검토와 4주 수요예측 |

### 4.3 문제 정의

핵심 문제는 “장비 목록이 없다”가 아니다.

1. 학교 공용 자산과 그룹 자산의 접근 범위가 다르다.
2. 대여 신청과 실물 반납 사이의 상태가 연결되지 않는다.
3. 반납 확인 없이 재고를 복구하면 실제 수량과 시스템 수량이 달라진다.
4. 미보유 장비는 그룹 필요성 검토와 학교 예산 검토가 모두 필요하다.
5. 관리자는 미래 부족을 알 수 없어 문제가 생긴 뒤에 대응한다.

따라서 Product Goal은 다음과 같이 정의한다.

> 학교와 소속 그룹의 자산을 하나의 흐름으로 대여·반납·도입하고, 관리자가 다음 4주 재고를 데이터로 준비하게 한다.

### 4.4 발표 시 사실 표현 주의

위 Pain Point는 대학 자산 운영 맥락을 바탕으로 세운 제품 가설이다. 실제 사용자 인터뷰를 하지 않았다면 “인터뷰 결과”라고 말하지 않는다. 실제 강사·동료 피드백이 있다면 TEAM INPUT으로 추가한다.

## 5. 템플릿을 현재 도메인으로 치환한 방법

| 제공 템플릿 | GearHub Campus | 현재 업무 의미 |
|---|---|---|
| Student | 일반 구성원 | 자산 대여·반납·도입 요청 |
| Instructor | 학교·그룹 관리자 | 자산·승인·예산·분석 운영 |
| Course | Asset | 학교 공용 또는 그룹 전용 장비 |
| Enrollment | Request | LOAN 또는 PURCHASE 요청 |
| Payment | Budget Review | 실제 카드 결제가 아닌 학교 예산 검토 |
| Recommend | Demand Analytics | 관리자용 4주 수요예측 |

이 매핑의 의미는 템플릿의 이름만 바꾼 것이 아니라 상태와 책임을 대학 자산 운영에 맞게 확장했다는 데 있다.

- courses에 수량, 소유 그룹, 공개 범위, 픽업 위치, 최대 대여일 추가
- enrollments에 그룹, 요청 유형, 기간, 반납, 검토자와 상태 추가
- payments를 도입 예산 검토 기록으로 사용
- 그룹·멤버십 및 분석 전용 테이블 추가
- 추천 서비스를 scikit-learn 기반 Demand Analytics로 확장

## 6. Product Backlog 설계

### 6.1 Backlog 작성 방식

PDF 교재의 흐름을 다음처럼 적용한다.

~~~text
비즈니스 문제
→ Epic
→ As a / I want / So that 형식의 User Story
→ MoSCoW 우선순위
→ Story Point
→ Acceptance Criteria
→ Sprint Backlog의 Task
~~~

상위 Story는 INVEST 관점으로 확인한다.

- Independent: 다른 Story와 가능한 한 독립적인가
- Negotiable: 구현 세부를 팀이 협의할 수 있는가
- Valuable: 사용자 또는 운영자 가치가 명확한가
- Estimable: 상대 크기를 추정할 수 있는가
- Small: 한 Sprint 안에서 끝낼 수 있는가
- Testable: 인수 조건으로 완료를 검증할 수 있는가

### 6.2 핵심 User Story

| ID | User Story | 우선순위 | Sprint |
|---|---|---|---|
| G-01 | 일반 구성원으로서 가입한 그룹을 보고 싶다. 그래야 사용할 수 있는 자산 범위를 알 수 있다. | Must | S1 |
| G-02 | 구성원으로서 초대코드로 그룹에 참여하고 싶다. 그래야 해당 그룹의 장비를 사용할 수 있다. | Must | S1 |
| A-01 | 구성원으로서 학교 공용 및 내 그룹 자산을 함께 보고 싶다. 그래야 대여 가능한 장비를 찾을 수 있다. | Must | S1 |
| L-01 | 구성원으로서 기간과 목적을 입력해 대여를 신청하고 싶다. 그래야 필요한 일정에 장비를 사용할 수 있다. | Must | S1 |
| L-02 | 그룹 관리자로서 재고가 있을 때 신청을 승인하고 싶다. 그래야 실제 가용 수량을 정확히 유지할 수 있다. | Must | S1 |
| L-03 | 구성원과 관리자로서 반납 요청과 실물 확인을 분리하고 싶다. 그래야 확인된 장비만 재고로 복구된다. | Must | S1 |
| Q-01 | 구성원으로서 그룹에 없는 장비 도입을 요청하고 싶다. 그래야 필요성을 공식적으로 검토받을 수 있다. | Must | S2 |
| Q-03 | 학교 관리자로서 그룹 승인된 요청의 예산을 검토하고 싶다. 그래야 학교 전체 예산을 통제할 수 있다. | Must | S2 |
| Q-04 | 그룹 관리자로서 승인된 장비의 입고를 확인하고 싶다. 그래야 실제 대여 가능한 자산으로 전환된다. | Must | S2 |
| D-03 | 학교 관리자로서 기준선과 후보 모델의 성능을 비교하고 싶다. 그래야 예측을 맹신하지 않을 수 있다. | Must | S2 |
| D-04 | 관리자로서 다음 4주 부족 수량과 이동 대안을 보고 싶다. 그래야 구매 전에 기존 재고를 재배치할 수 있다. | Must | S2 |
| O-01 | 시연자로서 전체 시스템을 한 번에 실행하고 싶다. 그래야 같은 환경에서 결과를 재현할 수 있다. | Must | S2 |

전체 Backlog와 Story Point는 [03_PRODUCT_BACKLOG.md](./03_PRODUCT_BACKLOG.md)를 기준으로 한다.

### 6.3 대표 Acceptance Criteria

#### 대여 승인

~~~gherkin
Given PENDING 대여 요청과 가용 재고가 있을 때
When 그룹 관리자가 승인한다
Then 요청은 ACTIVE가 된다
And availableQuantity는 정확히 1 감소한다
~~~

#### 반납 확인

~~~gherkin
Given 요청이 RETURN_REQUESTED일 때
When 그룹 관리자가 실물을 확인한다
Then 요청은 RETURNED가 된다
And availableQuantity는 정확히 1 복구된다
~~~

#### 도입·예산

~~~gherkin
Given 그룹 관리자가 PURCHASE 요청을 승인했을 때
When 학교 관리자가 Budget를 승인한다
Then payment.completed가 발행된다
And Request는 BUDGET_APPROVED가 된다
~~~

#### AI 예측

~~~gherkin
Given 시간순으로 정렬된 주간 대여 이력이 있을 때
When 모델을 학습하고 그룹 분석 화면을 연다
Then 같은 테스트 구간의 기준선과 모델 WAPE를 비교할 수 있다
And 다음 4주의 필요 수량·부족 수량·이동 제안을 볼 수 있다
~~~

## 7. Sprint 1 설계와 결과

### 7.1 Sprint Goal

> 구성원이 자신의 그룹에서 사용할 수 있는 장비를 신청하고, 관리자가 승인·반납 확인했을 때 재고가 정확히 감소·복구된다.

### 7.2 왜 Sprint 1에 넣었는가

대여 서비스가 사용자에게 가치를 주려면 검색이나 신청 버튼만 있어서는 안 된다. 그룹 접근 범위, 대여 요청, 관리자 승인, 실물 반납 확인, 재고 원복이 하나의 흐름으로 끝나야 한다.

Sprint 1은 PDF의 Walking Skeleton 원칙에 맞춰 핵심 도메인 서비스 3개에 집중했다.

| 서비스 | Sprint 1 책임 |
|---|---|
| Member | 사용자, 그룹, 멤버십, 관리자 권한 |
| Asset | 학교 공용·그룹 전용 자산, 가용 수량 |
| Request | 대여 신청, 승인·반려, 반납 요청·확인 |

Auth, Gateway, Eureka는 제공 인프라를 사용하고 수정 범위에서 제외했다.

### 7.3 Sprint Backlog

| 범위 | Story | 결과 |
|---|---|---|
| 인증·그룹 | F-01, G-01~02 | 로그인, 그룹 목록, 초대 가입, 역할 |
| 자산 | A-01~02 | 공용·그룹 자산, 수량·위치·대여일 |
| 대여 | L-01~04 | 신청, 승인·반려, 반납, 연체 |

최종 상대 추정치는 47 SP다. 이는 실제 시간 합계가 아니라 Story 간 크기 비교다.

### 7.4 Walking Skeleton

~~~text
로그인
→ 내 그룹 선택
→ 학교 공용 + 그룹 전용 자산 조회
→ 기간·사유로 대여 신청 PENDING
→ 관리자 승인 ACTIVE / 재고 -1
→ 구성원 반납 요청 RETURN_REQUESTED
→ 관리자 실물 확인 RETURNED / 재고 +1
~~~

### 7.5 역할별 산출물

| 역할 | Sprint 1 산출물 |
|---|---|
| 기획 | Stakeholder·Pain Point, 도메인 매핑, Product Goal |
| 백엔드 | 그룹·자산·대여 API와 상태·재고 규칙 |
| 프론트엔드 | 그룹, 자산, 대여, 반납 화면 |
| 품질 | 인수 조건, Spring 테스트, 전후 재고 검증 |

### 7.6 Sprint Review

시연할 질문:

1. 사용자가 볼 수 있는 자산 범위가 그룹 권한과 맞는가
2. 승인 전후 같은 Asset의 availableQuantity가 1 감소하는가
3. 반납 요청만으로 재고가 복구되지 않는가
4. 관리자 실물 확인 후 정확히 1 복구되는가

Review 결과: 그룹 대여·반납의 수직 흐름과 재고 정합성을 확인해 Sprint Goal을 충족했다.

## 8. Sprint 2 설계와 결과

### 8.1 Sprint Goal

> 그룹에 없는 장비를 도입 요청해 예산 승인과 입고까지 추적하고, 관리자가 다음 4주 부족 재고와 이동 대안을 확인한다.

### 8.2 Sprint 2로 미룬 이유

대여·반납만으로도 Sprint 1의 사용자 가치는 성립한다. Budget, Kafka, Analytics는 그 핵심 흐름을 확장해 조직 승인과 미래 의사결정을 자동화하므로 Sprint 2에 배치했다.

| 확장 영역 | 이유 |
|---|---|
| 도입·예산 | 그룹 필요성 판단과 학교 예산 책임을 분리 |
| Kafka | Budget 결과와 대여 이력을 서비스 간 느슨하게 전달 |
| 입고 | 승인 문서를 실제 대여 가능한 자산으로 전환 |
| Demand Analytics | 과거 기록을 미래 재고 결정에 활용 |
| Docker·시드 | 전체 Increment를 재현 가능한 상태로 전달 |

### 8.3 Backlog Refinement에서의 AI 결정

팀 내부 문제 검토에서 대여자 개인 추천은 우선순위가 낮다고 판단했다. 대여자는 이미 필요한 장비를 정하고 들어오는 경우가 많기 때문이다. 단순 외부 AI API 호출도 자체 데이터를 분석한 근거가 약하다.

따라서 AI Story를 다음처럼 다시 정의했다.

~~~text
개인에게 무엇을 빌릴지 추천
        ↓ 우선순위에서 제외
관리자에게 다음 4주 부족 재고를 예측
        ↓
그룹 간 이동 또는 신규 도입 결정
~~~

이 결정은 계획을 끝까지 고집하기보다 사용자 가치와 데이터 활용 가능성에 따라 Backlog를 정제한 Agile 사례로 설명한다. 외부 이해관계자 피드백으로 표현하지 않고 팀 내부 Refinement 결과로 표현한다.

### 8.4 Sprint Backlog

| 범위 | Story | 결과 |
|---|---|---|
| 도입·예산·입고 | Q-01~04 | 그룹 승인, Budget, Kafka, 자산 전환 |
| 데이터·AI | D-01~05 | LIVE 이벤트, 시뮬레이션, 모델, 대시보드 |
| 전달 품질 | O-01~03 | Compose, 고정 시드, 테스트·문서 |

최종 상대 추정치는 69 SP다.

### 8.5 Sprint 2 전체 흐름

~~~text
도입 요청 PENDING
→ 그룹 승인 GROUP_APPROVED
→ Budget PENDING
→ 학교 예산 승인 COMPLETED
→ Kafka payment.completed
→ Request BUDGET_APPROVED
→ 입고 RECEIVED
→ Asset OWNED / ACTIVE

대여 요청
→ Kafka rental.lifecycle
→ Analytics LIVE 이벤트
→ 모델 학습·평가
→ 4주 수요·필요 수량·부족·이동 제안
~~~

### 8.6 역할별 산출물

| 역할 | Sprint 2 산출물 |
|---|---|
| 기획 | AI 문제 재정의, 우선순위 조정, 관리자 행동 정의 |
| 백엔드 | Budget·Kafka·입고 API와 상태 전이 |
| AI·데이터 | 시뮬레이션, 모델 비교, 예측·재고 계산 |
| 프론트엔드 | 관리자 승인·입고·Analytics 화면 |
| 품질 | 통합 테스트, DB 수치, Docker·브라우저 검증 |

### 8.7 Sprint Review

Review에서 보여줄 것:

1. 도입 요청이 그룹 승인과 학교 예산 승인으로 분리되는가
2. payment.completed 이후 Request가 자동으로 바뀌는가
3. 입고 후 PURCHASE_REQUEST가 OWNED / ACTIVE로 전환되는가
4. 모델이 기준선보다 나은지 수치로 확인되는가
5. 예측이 차트에서 끝나지 않고 이동·도입 판단으로 이어지는가

Review 결과: 도입·예산·입고와 관리자 수요예측을 실제 서비스와 화면으로 연결해 Sprint Goal을 충족했다.

## 9. MSA 아키텍처 설명

### 9.1 서비스 책임

| 서비스 | 포트 | 소유 데이터 | 책임 |
|---|---:|---|---|
| Member | 8081 | users, campus_groups, group_memberships | 사용자·그룹·권한 |
| Asset | 8082 | courses | 자산·재고·입고 |
| Request | 8083 | enrollments | 대여·반납·도입 상태 |
| Budget | 8084 | payments | 학교 예산 검토 |
| Demand Analytics | 8085 | analytics_* | 이벤트·학습·예측 |

### 9.2 발표용 구성도

~~~mermaid
flowchart LR
    U[사용자] --> W[Vue Web]
    W --> G[Gateway]
    G --> M[Member]
    G --> A[Asset]
    G --> R[Request]
    G --> B[Budget]
    G --> D[Demand Analytics]

    R -->|권한 확인| M
    R -->|재고 차감·복구| A
    R -->|예산 요청| B
    B -->|payment.completed| K[Kafka]
    K --> R
    R -->|rental.lifecycle| K
    K --> D
    D -->|현재 재고| A

    M --> DB[(MariaDB)]
    A --> DB
    R --> DB
    B --> DB
    D --> DB
~~~

### 9.3 MSA를 유지한 이유

- 그룹·권한, 재고, 요청, 예산, 분석은 변경 이유가 다르다.
- Budget와 Analytics를 추가해도 Sprint 1의 대여 API를 전면 수정할 필요가 없다.
- API 계약을 기준으로 백엔드와 프론트엔드가 병렬 작업할 수 있다.
- AI 모델은 Python 서비스 안에서 교체할 수 있다.

동시에 MSA가 무조건 정답이라고 주장하지 않는다. 현재 실습은 제공된 구조를 활용했고, 실제 소규모 제품이라면 모듈러 모놀리식과 운영 비용을 비교해야 한다.

### 9.4 발표에서 보여줄 API

| 흐름 | Method·Path | 변화 |
|---|---|---|
| 대여 신청 | POST /api/enrollments | PENDING 생성 |
| 대여 승인 | POST /api/enrollments/{id}/approve | ACTIVE, 재고 -1 |
| 반납 요청 | POST /api/enrollments/{id}/return-request | RETURN_REQUESTED |
| 반납 확인 | POST /api/enrollments/{id}/return-confirm | RETURNED, 재고 +1 |
| 도입 요청 | POST /api/enrollments/acquisitions | PURCHASE 생성 |
| 그룹 승인 | POST /api/enrollments/{id}/group-approve | GROUP_APPROVED |
| 예산 승인 | POST /api/payments/{id}/approve | COMPLETED 이벤트 |
| 입고 | POST /api/enrollments/{id}/receive | RECEIVED, OWNED |
| AI 평가 | GET /api/recommend/analytics/evaluation | 기준선·모델 지표 |
| AI 예측 | GET /api/recommend/analytics/forecast?groupId={id} | 4주 수요·부족·이동 |

전체 계약은 [06_API_CONTRACT.md](./06_API_CONTRACT.md)를 기준으로 한다.

## 10. AI가 왜 필요한가

### 10.1 AI가 없는 시스템

대여·반납 시스템만 있어도 현재 재고와 과거 요청은 관리할 수 있다. 그러나 관리자는 부족이 실제로 발생한 뒤에야 대응한다.

~~~text
현재 수량 확인
→ 품절 발생
→ 급하게 다른 그룹에 문의
→ 해결되지 않으면 뒤늦게 도입 요청
~~~

### 10.2 AI가 추가하는 가치

AI는 거래 기능을 대신하지 않는다. 거래에서 축적된 데이터를 미래 의사결정으로 전환한다.

~~~text
대여 이력
→ 다음 4주 수요예측
→ 평균 대여기간으로 필요 수량 계산
→ 현재 재고와 비교
→ 그룹 간 이동 우선
→ 부족이 남으면 도입 검토
~~~

따라서 AI는 대여 트랜잭션 자체에 필수는 아니지만 “문제가 생기기 전에 재고를 준비한다”는 관리자 Pain Point를 해결하는 데 필수다. 이 구분을 솔직히 설명하면 억지로 AI를 붙였다는 인상을 줄일 수 있다.

### 10.3 외부 AI API를 사용하지 않은 이유

- 생성형 API의 문장 생성은 재고 의사결정 근거가 아니다.
- 개인 추천은 사용자가 이미 품목을 정하고 오는 상황에서 가치가 낮다.
- 자체 대여 데이터로 기준선과 모델을 비교해야 분석 기능의 의미를 증명할 수 있다.

## 11. AI 기능과 검증 방법

### 11.1 데이터

| 항목 | 값 |
|---|---:|
| 집계 단위 | 그룹 × 카테고리 × 주 |
| 이력 기간 | 78주 |
| 분석 그룹 | 8 |
| 카테고리 | 7 |
| SIMULATION 이벤트 | 10,814 |
| 운영 화면용 대여 | 200 |
| 예측 기간 | 다음 4주 |

SIMULATION 데이터와 Kafka의 LIVE 이벤트를 분리한다. 현재 결과는 파이프라인과 평가 방법을 검증하기 위한 합성 이력이며 실제 운영 정확도로 과장하지 않는다.

### 11.2 특징과 후보 모델

특징:

- group_id, category
- month, week_of_year, week_sin, week_cos
- lag 1·2·4주
- 최근 4주·8주 이동평균

비교:

- 기준선: 최근 4주 이동평균
- Poisson Regressor
- Random Forest Regressor
- Histogram Gradient Boosting Regressor

랜덤 분할이 아니라 과거 학습 → 다음 검증 → 마지막 테스트의 시간순 분할을 사용한다.

### 11.3 평가 결과

| 지표 | 기준선 | 선택 모델 |
|---|---:|---:|
| MAE | 1.4717 | 1.1657 |
| WAPE | 70.3414% | 55.7159% |

선택 모델: Histogram Gradient Boosting  
기준선 대비 WAPE 개선율: 20.79%

발표에서는 정확도가 높다고만 말하지 않는다. “같은 테스트 구간에서 단순한 최근 4주 평균보다 나았고, 오차도 함께 공개했다”고 설명한다.

### 11.4 관리자에게 보여주는 결과

필요 수량:

    requiredUnits = ceil(최대 주간 예측 × 평균 대여일 / 7 × 1.15)

부족 수량:

    shortageUnits = max(0, 필요 수량 - 그룹 재고 - 배분된 학교 공용 재고)

화면 출력:

- 4주 주간 예측
- 평균 대여기간
- 필요 수량
- 총재고와 현재 가용재고
- 부족 수량과 LOW·MEDIUM·HIGH 위험도
- 다른 그룹의 계획상 이동 가능 수량

## 12. 구현·검증 근거

| 검증 | 결과 |
|---|---|
| Docker Compose | Web 포함 11개 컨테이너 running |
| 그룹 | 8개 |
| 자산 | 120개 |
| 운영 대여 | 200건 |
| 도입 요청 | 8건 |
| 분석 이벤트 | SIMULATION 10,814건, LIVE 1건 |
| 최신 예측 | 224행 |
| 비정상 재고 | 0건 |
| Spring 서비스 | Member·Asset·Request·Budget 테스트 성공 |
| Analytics | 4 passed |
| Frontend | Vite 125 modules, production audit 0 |
| 브라우저 | 로그인·그룹·관리자·AI 화면 확인 |

상세 근거는 [10_FINAL_VALIDATION.md](./10_FINAL_VALIDATION.md)를 기준으로 한다.

## 13. 15분 PPT 구성표

총 발표시간은 정확히 15분, 13장 기준이다.

| 장 | 시간 | 누적 | 제목 | 핵심 메시지 |
|---:|---:|---:|---|---|
| 1 | 0:30 | 0:30 | GearHub Campus | 한 학교·멀티그룹 자산 운영과 수요예측 |
| 2 | 1:00 | 1:30 | 이해관계자와 Pain Point | 문제는 목록이 아니라 끊어진 운영 흐름 |
| 3 | 0:50 | 2:20 | 제품 목표와 도메인 | 대여·반납·도입·예측의 하나의 흐름 |
| 4 | 1:10 | 3:30 | Agile·Backlog 설계 | 사용자 가치로 우선순위와 AC 결정 |
| 5 | 1:20 | 4:50 | Sprint 1 Walking Skeleton | 그룹 대여·반납과 재고 정합성 |
| 6 | 1:20 | 6:10 | Sprint 2 확장 | 도입·Budget·Kafka·AI |
| 7 | 1:10 | 7:20 | MSA 아키텍처 | 서비스 책임, REST와 이벤트 |
| 8 | 0:50 | 8:10 | AI가 필요한 이유 | 사후 기록을 사전 재고 결정으로 전환 |
| 9 | 1:20 | 9:30 | 데이터·모델·평가 | 시간순 비교와 기준선 대비 20.79% 개선 |
| 10 | 0:50 | 10:20 | 예측에서 행동으로 | 필요 수량·부족·그룹 간 이동 |
| 11 | 3:10 | 13:30 | Live Demo | 상태와 재고의 전후 변화를 직접 확인 |
| 12 | 1:00 | 14:30 | 검증·회고·한계 | Done 근거와 과장하지 않는 한계 |
| 13 | 0:30 | 15:00 | 결론 | Agile 설계 + MSA 실행 + 행동 가능한 AI |

## 14. 슬라이드별 PPT 뼈대

### Slide 1. GearHub Campus

화면:

- 제품명
- 한 문장 소개
- 팀명·팀원 TEAM INPUT

발표 문장:

> 저희는 학교와 학과·연구실·동아리에 흩어진 장비를 대여·반납·도입하고, 다음 4주 부족 재고까지 예측하는 GearHub Campus를 만들었습니다.

### Slide 2. 이해관계자와 Pain Point

화면:

- 구성원, 그룹 관리자, 학교 관리자 3명
- 각 사용자 아래 한 줄 Pain Point
- “흩어진 목록”보다 “끊어진 흐름”을 강조

발표 문장:

> 구성원은 무엇을 빌릴 수 있는지 찾기 어렵고, 그룹 관리자는 반납과 재고를 수기로 맞추며, 학교 관리자는 부족이 발생한 뒤에야 도입을 검토합니다.

### Slide 3. 제품 목표와 핵심 흐름

화면:

~~~text
그룹 참여
→ 자산 조회
→ 대여·반납
→ 미보유 장비 도입
→ 4주 수요예측
~~~

발표 문장:

> 한 학교 안의 여러 그룹을 별도 서버로 나누지 않고, 하나의 동적 워크스페이스에서 학교 공용 자산과 그룹 자산을 함께 운영하도록 설계했습니다.

### Slide 4. Agile과 Product Backlog

화면:

- Epic → User Story → AC → Sprint Backlog
- Must·Should·Could
- 대표 User Story 2개

발표 문장:

> 기능 목록을 서비스별로 나누지 않고, 누가 어떤 가치를 얻는지 User Story로 작성한 뒤 재고 정합성과 사용자 흐름을 Must로 우선했습니다.

### Slide 5. Sprint 1 Walking Skeleton

화면:

- Sprint Goal
- Member·Asset·Request 3개 서비스
- PENDING → ACTIVE → RETURN_REQUESTED → RETURNED
- 재고 -1 → 유지 → +1

발표 문장:

> Sprint 1에서는 넓게 조금씩 만드는 대신 그룹 선택부터 대여, 실물 반납, 재고 복구까지 실제 사용자가 끝낼 수 있는 한 흐름을 완성했습니다.

### Slide 6. Sprint 2 확장과 Refinement

화면:

- 도입 → 그룹 승인 → Budget → Kafka → 입고
- 개인 추천 제외 → 관리자 수요예측
- Sprint 1과 Sprint 2 산출물 비교

발표 문장:

> Sprint 2에서는 조직 승인과 이벤트 연동을 붙였습니다. 또한 사용자가 이미 필요한 품목을 안다는 점을 검토해 개인 추천보다 관리자 수요예측이 더 가치 있다고 판단했습니다.

### Slide 7. MSA 아키텍처

화면:

- Member·Asset·Request·Budget·Analytics 구성도
- REST는 실선, Kafka는 점선 또는 다른 색
- 각 서비스 아래 소유 테이블

발표 문장:

> 즉시 확인이 필요한 권한과 재고는 REST로, Budget 결과와 분석 이력은 Kafka로 연결했습니다. 요청 상태와 재고 상태는 각각 Request와 Asset이 소유합니다.

### Slide 8. AI가 필요한 이유

화면:

| AI 없음 | AI 있음 |
|---|---|
| 현재 수량·과거 기록 | 다음 4주 수요 |
| 품절 후 대응 | 부족 전 이동·도입 |
| 경험에 의존 | 기준선과 오차 공개 |

발표 문장:

> AI는 대여 기능을 대신하지 않습니다. 운영 데이터가 쌓여도 미래를 준비하지 못하는 문제를 해결해, 사후 대응을 사전 의사결정으로 바꿉니다.

### Slide 9. 데이터·모델·평가

화면:

- 78주, 10,814 이벤트
- 시간순 Train·Validation·Test
- 기준선과 모델 WAPE 막대그래프

그래프 값:

- Baseline 70.3414
- Model 55.7159
- Improvement 20.79%

발표 문장:

> 랜덤 분할로 미래 정보가 새지 않도록 시간순으로 평가했고, Poisson·Random Forest·Histogram Gradient Boosting을 비교해 단순 최근 4주 평균보다 나은지 확인했습니다.

### Slide 10. 예측을 관리자 행동으로

화면:

- 카테고리별 4주 예측
- 필요 수량·재고·부족
- 다른 그룹 이동 제안

발표 문장:

> 예측값만 보여주지 않고 평균 대여기간으로 필요한 동시 수량을 계산하고, 공용 재고와 그룹 재고를 비교해 먼저 이동을 제안한 뒤 부족이 남을 때 도입을 검토하도록 했습니다.

### Slide 11. Live Demo

화면:

- 실제 브라우저
- 요청 전후 동일 Asset의 수량
- 관리자 Analytics

말보다 화면 변화를 우선한다. 상세 순서는 15절을 따른다.

### Slide 12. 검증·회고·한계

화면:

- 11 containers
- Tests PASS
- Keep·Problem·Try 각 1줄
- 합성 데이터와 미구현 범위

발표 문장:

> Done을 코드 작성으로 보지 않고 Docker 실행, 상태·재고 통합 검증, 자동 테스트와 문서까지 포함했습니다. 다만 AI는 합성 이력 기반이며 실제 운영 데이터 재검증이 필요합니다.

### Slide 13. 결론

화면:

~~~text
Agile: 가치에 따라 범위를 나눔
MSA: 변경 가능한 서비스 경계
AI: 데이터에서 관리자 행동 도출
~~~

발표 문장:

> GearHub Campus의 핵심은 기능 수가 아니라, 사용자 가치로 Sprint를 설계하고 MSA로 흐름을 연결하며 AI 결과를 실제 재고 행동으로 만든 점입니다.

## 15. 3분 10초 Live Demo Runbook

### 15.1 데모 전 준비

1. 발표 10분 전에 시드를 한 번 실행한다.
2. Docker 11개 컨테이너와 Eureka 등록을 확인한다.
3. 관리자 계정으로 로그인한 브라우저를 준비한다.
4. /groups/1/assets, /groups/1/admin, /groups/1/analytics 탭을 미리 연다.
5. PENDING 대여와 RETURN_REQUESTED 대여를 각각 한 건 선택한다.
6. 모델 재학습은 발표 중 실행하지 않고 저장된 평가 결과를 사용한다.
7. 같은 장면의 스크린샷을 백업으로 준비한다.

테스트 계정:

| 역할 | 이메일 | 비밀번호 |
|---|---|---|
| 학교·그룹 관리자 | campus.admin@demo.local | GearHub123! |
| 일반 구성원 | campus.member@demo.local | GearHub123! |

### 15.2 실제 순서

| 구간 | 시간 | 동작 | 반드시 말할 것 |
|---|---:|---|---|
| 그룹·자산 | 0:00~0:30 | 8개 그룹과 컴퓨터공학과 자산을 연다 | 그룹별 서버가 아닌 동적 URL |
| 대여 승인 | 0:30~1:05 | PENDING 승인, 같은 Asset 수량 확인 | ACTIVE와 재고 -1 |
| 반납 확인 | 1:05~1:40 | RETURN_REQUESTED 확인 처리 | 요청 때 유지, 확인 때 +1 |
| 도입 | 1:40~2:10 | 도입·Budget·입고 상태를 보여준다 | 두 단계 승인과 Kafka |
| AI 평가 | 2:10~2:35 | 기준선·모델 WAPE를 보여준다 | 오차와 비교 기준 공개 |
| AI 행동 | 2:35~3:10 | 부족 카테고리와 이동 제안을 연다 | 예측 → 이동 → 도입 |

### 15.3 데모 실패 시 대체

- 브라우저 오류: 전후 스크린샷으로 같은 상태 변화를 설명한다.
- Kafka 지연: Budget와 Request 상태 화면을 새로고침하고 이벤트 구조 슬라이드로 전환한다.
- AI API 오류: 저장된 평가 표와 Dashboard 스크린샷을 사용한다.
- 시간이 부족하면 도입의 클릭 시연은 생략하고 상태 다이어그램으로 설명한다.

데모 실패를 숨기지 않는다. Sprint Review 원칙에 따라 무엇이 완료됐고 어떤 연결에서 문제가 났는지 짧게 설명한다.

## 16. 보고서·PPT에 넣을 화면과 도표

### 필수

- 3명의 이해관계자와 Pain Point
- 템플릿 → GearHub 도메인 매핑표
- Product Backlog 일부와 Sprint 배치
- Sprint 1 상태·재고 흐름
- Sprint 2 도입·예산·입고 흐름
- MSA 구성도
- 기준선 대 모델 WAPE 비교 그래프
- AI 부족·이동 제안 화면
- 대여 승인 전·후 재고
- 반납 요청·확인 전후 재고
- Docker 또는 Eureka 실행 근거

### 화면 캡처 상태

실제 이미지 파일은 아직 저장소에 추가되지 않았다. [07_VALIDATION_AND_DEMO.md](./07_VALIDATION_AND_DEMO.md)의 파일명 기준으로 TEAM INPUT이 필요하다.

## 17. Review와 Retrospective

### Review에서 답할 질문

- Sprint Goal을 실제 동작으로 증명했는가
- 계획한 User Story 중 무엇이 Done인가
- 요청 전후 상태와 재고가 보이는가
- AI가 기준선보다 나은가
- AI 결과로 관리자가 무엇을 할 수 있는가

### Retrospective 초안

Keep:

- 사용자 흐름 단위의 Walking Skeleton
- 운영 데이터와 분석 데이터 분리
- 기준선·시간순 평가·오차 공개
- 고정 시드와 Compose 재현

Problem:

- 물리 이름 course·enrollment·payment와 업무 용어가 다름
- 서비스 간 분산 트랜잭션 보상 미구현
- AI가 합성 이력 기반
- 제출용 화면 캡처 미완료

Try:

- 발표에서는 업무 이름을 먼저 쓰고 물리 이름은 한 번만 매핑
- 실제 LIVE 데이터의 월별 WAPE 추적
- Outbox·Saga·내부 서비스 인증 Spike
- 요청 전후 캡처와 실제 팀 피드백 추가

## 18. 발표에서 피해야 할 표현

- “대학교 사용자를 인터뷰했다” - 실제 인터뷰가 없다면 사용 금지
- “AI 정확도가 80%다” - 현재 지표는 정확도가 아니라 WAPE
- “실제 데이터 10,814건이다” - SIMULATION 합성 이력이라고 명시
- “완전한 멀티테넌트 SaaS다” - 현재는 한 학교·멀티그룹
- “AI가 대여할 물건을 추천한다” - 관리자 수요예측이 핵심
- “Kafka가 분산 트랜잭션을 완전히 해결한다” - 보상 설계는 후속 범위
- “MSA가 항상 최선이다” - 제공 구조와 변경 경계에 맞춘 선택
- 컨테이너 수와 코드량만 나열 - 먼저 사용자 가치와 상태 변화를 설명

## 19. 최종 제출 체크리스트

### 보고서

- [ ] 팀명·팀원·역할 입력
- [ ] 이해관계자와 Pain Point를 한 페이지 안에 정리
- [ ] 도메인 매핑표 포함
- [ ] Product Goal과 MVP 범위 포함
- [ ] User Story가 As a / I want / So that 구조인가
- [ ] Sprint Goal과 Sprint Backlog가 구분되는가
- [ ] Sprint 1·2를 나눈 이유가 사용자 가치로 설명되는가
- [ ] MSA 구성도에 REST·Kafka 흐름이 표시되는가
- [ ] 핵심 API Method·URL·상태 변화가 있는가
- [ ] AI 데이터·기준선·분할·모델·오차·행동이 모두 있는가
- [ ] 합성 데이터와 보안·분산 일관성 한계가 있는가
- [ ] Review·Retro와 다음 Backlog가 있는가

### PPT

- [ ] 13장, 총 15분으로 리허설
- [ ] 슬라이드마다 핵심 문장 하나
- [ ] 글보다 상태 흐름·도표·스크린샷 중심
- [ ] WAPE 그래프 축과 값 확인
- [ ] Demo 탭과 테스트 계정 준비
- [ ] 전후 화면의 Asset ID와 수량이 동일한지 확인
- [ ] 백업 스크린샷 준비
- [ ] 실제 팀 피드백 TEAM INPUT 추가

## 20. 상세 문서 연결

| 필요 내용 | 상세 문서 |
|---|---|
| 제품·이해관계자·MVP | [02_PRODUCT_AND_DOMAIN.md](./02_PRODUCT_AND_DOMAIN.md) |
| 전체 Backlog·AC·DoD | [03_PRODUCT_BACKLOG.md](./03_PRODUCT_BACKLOG.md) |
| Sprint 작업 분해 | [04_TWO_DAY_SPRINT_PLAN.md](./04_TWO_DAY_SPRINT_PLAN.md) |
| 아키텍처·ERD·시퀀스 | [05_ARCHITECTURE_AND_ERD.md](./05_ARCHITECTURE_AND_ERD.md) |
| API | [06_API_CONTRACT.md](./06_API_CONTRACT.md) |
| 데모·스크린샷 | [07_VALIDATION_AND_DEMO.md](./07_VALIDATION_AND_DEMO.md) |
| Agile 실행·회고 | [08_AGILE_EXECUTION_LOG.md](./08_AGILE_EXECUTION_LOG.md) |
| AI 설계 | [09_CAMPUS_PIVOT_AND_AI.md](./09_CAMPUS_PIVOT_AND_AI.md) |
| 실행 검증 | [10_FINAL_VALIDATION.md](./10_FINAL_VALIDATION.md) |

## 21. PDF 근거 페이지

PDF의 지시를 그대로 작업 명령으로 사용한 것이 아니라, 발표·보고서 평가 구조를 추출해 현재 프로젝트에 적용했다.

| 자료 | 참고 범위 |
|---|---|
| 가이드1. Agile MSA 진행 가이드 | 1~3쪽: Walking Skeleton, Sprint 2 확장, Review·Retro |
| 가이드2. Agile MSA 실습 가이드 | 1~6쪽: 이해관계자 가치, 도메인 매핑, Sprint 구분, 발표 필수 6단계 |
| 가이드3. 코드 템플릿 설명 문서 | 1~4쪽, 22~24쪽: 서비스 경계, REST·Kafka, API·구성도 |
| 보강1. Agile MSA 사례로 살펴보는 이해 | 21~27쪽: 피드백, 역할별 산출물, 실제 연결과 자가진단 |
| 교재. Cloud Agile 방법론 및 MSA 개발 | 17~21쪽, 28~30쪽, 43쪽, 89~92쪽: User Story·INVEST·Sprint Planning·Review·Retro |
