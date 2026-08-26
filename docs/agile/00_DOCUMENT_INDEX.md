# GearHub Campus 실습 문서 안내

## 문서 작성 전제

- 실습 기간은 2일이며, 하루를 하나의 스프린트로 운영한다.
- Sprint 1은 `Member + Asset + Request`를 이용한 대여 워킹 스켈레톤을 완성한다.
- Sprint 2는 `Budget + Kafka + Alternative`를 붙여 신규 구매요청까지 확장한다.
- Auth Server, API Gateway, Eureka Server는 제공 인프라로 보고 내부 구현을 수정하지 않는다.
- 기존 템플릿의 포트, API 경로, Kafka 토픽과 물리 테이블은 호환성을 위해 유지한다.
- 발표 장표는 이 문서 세트에 포함하지 않는다.
- 현재 코드를 기준으로 확인된 사실과 향후 제안을 구분하며, 수행하지 않은 회의나 사용자 검증을 수행한 것처럼 기록하지 않는다.
- 제품은 대학교의 내부 장비 운영을 지원하는 B2B SaaS로 정의하며, 현재 구현은 한 학교 안의 학과·연구실·동아리를 분리하는 멀티그룹 MVP로 설명한다.
- 01~08은 초기 단일 조직·대체재 아이디어에서 발전해 온 설계·실행 기록이다. 최종 구현과 충돌하는 내용은 09~10과 루트 README를 기준으로 해석한다.

## 문서 목록

| 문서 | 용도 | 가이드 요구사항 |
|---|---|---|
| [01_TEMPLATE_SUBNOTE.md](./01_TEMPLATE_SUBNOTE.md) | 제공 템플릿 이해, 변경 범위와 시행착오 기록 | 가이드 1의 개인 서브노트 |
| [02_PRODUCT_AND_DOMAIN.md](./02_PRODUCT_AND_DOMAIN.md) | 이해관계자, Pain Point, 솔루션, 도메인 매핑과 AI 역할 | 가이드 2의 기획 흐름 1~2 |
| [03_PRODUCT_BACKLOG.md](./03_PRODUCT_BACKLOG.md) | 우선순위가 있는 사용자 스토리와 완료 조건 | 가이드 2의 Backlog·User Story·DoD |
| [04_TWO_DAY_SPRINT_PLAN.md](./04_TWO_DAY_SPRINT_PLAN.md) | Day 1·2 계획, 역할, Review와 Retro | 가이드 1·2의 Sprint Planning/Review/Retro |
| [05_ARCHITECTURE_AND_ERD.md](./05_ARCHITECTURE_AND_ERD.md) | 서비스 경계, REST·Kafka 흐름, 시퀀스와 ERD | 가이드 2의 아키텍처, 가이드 3 Step 1~5·7 |
| [06_API_CONTRACT.md](./06_API_CONTRACT.md) | 프론트엔드와 서비스 간 API 계약 및 예시 | 가이드 2·3의 API 명세 |
| [07_VALIDATION_AND_DEMO.md](./07_VALIDATION_AND_DEMO.md) | 인수 테스트 결과, 캡처 목록과 데모 순서 | 가이드 2의 동작 화면·통합 검증 |
| [08_AGILE_EXECUTION_LOG.md](./08_AGILE_EXECUTION_LOG.md) | Sprint 0, 보드, 속도, 계획 대비 실적과 회고 액션 | 추가 Agile·MSA PDF의 Scrum 실행 증거 |
| [09_CAMPUS_PIVOT_AND_AI.md](./09_CAMPUS_PIVOT_AND_AI.md) | 대학교 멀티그룹 전환, 완성된 업무 흐름, 관리자 AI 설계와 배운 점 | 최종 제품·AI 설계 기준 |
| [10_FINAL_VALIDATION.md](./10_FINAL_VALIDATION.md) | Docker, 데이터, E2E, 테스트, 모델 성능과 발표 데모 증거 | 최종 검증 기준 |

서비스 전체 요약과 실행 방법은 루트 [readme.md](../../readme.md)와 상위 문서 [SKALA_GEARHUB.md](../SKALA_GEARHUB.md)를 사용한다.

## 가이드 요구사항 대응표

| 출처 | 핵심 요구 | GearHub 대응 |
|---|---|---|
| 가이드 1 | Sprint 1은 2~3개 도메인 서비스 안에서 처음부터 끝까지 동작하는 한 흐름을 만든다. | 대여 가능한 교보재 조회 → 대여 신청 → 운영진 승인 → 재고 차감을 Sprint 1 목표로 정의 |
| 가이드 1 | Sprint 2에서 Payment, Kafka, Recommend를 팀 도메인에 맞게 확장한다. | Payment를 Budget으로, Recommend를 Alternative로 해석하고 예산 결과를 Kafka로 Request에 반영 |
| 가이드 1 | Sprint마다 짧은 데모와 회고를 수행한다. | 각 스프린트에 Review 시나리오와 Retro 항목을 배치 |
| 가이드 1 | Auth Server와 API Gateway는 제공 이미지이며 수정 대상이 아니다. | 이미지 내부 코드는 변경하지 않고 새 서비스 목적지는 Compose 환경변수로 연결 |
| 가이드 2 | 이해관계자 가치 → 솔루션 → 스프린트 → 아키텍처 → API → 화면 순서로 설명한다. | 문서 번호 자체를 같은 설명 순서로 구성 |
| 가이드 2 | 팀 아이디어로 도메인 매핑표를 먼저 작성한다. | Course→Asset, Enrollment→Request, Payment→Budget 등 명시 |
| 가이드 2 | 프론트는 Gateway를 호출하고 토큰을 포함한다. | Base URL `http://localhost:8080`, OAuth2 토큰과 Axios 인터셉터 사용 |
| 가이드 3 | 요구사항, 도메인, 인프라, 통신, ERD, API, Docker 구성을 설계한다. | 아키텍처·ERD 문서와 API 계약 문서에 현재 구현 기준으로 재작성 |
| 보강 1 | MSA가 항상 정답은 아니며, 실제 API 연결·서비스 독립성·외부 피드백을 증명한다. | 실습에서 MSA를 택한 이유와 한계를 기록하고 Mock이 아닌 API 검증·독립성 실험 항목 추가 |
| 보강 2 | 이미지·컨테이너·Compose를 구분하고 멀티스테이지·네트워크·환경변수·최소 권한을 점검한다. | 현재 Dockerfile·Compose의 충족 항목과 보안·운영 보강점을 서브노트에 기록 |
| 추가 PDF | Product/Sprint Backlog, Increment, DoD와 Planning·Daily·Review·Retro를 실제 운영 흔적으로 남긴다. | Sprint Board, 속도, Review 피드백란, Keep·Problem·Try 액션 추적 문서 추가 |
| 추가 PDF | Epic → User Story → Task로 분해하고 INVEST·MoSCoW·Planning Poker를 활용한다. | 상위 백로그 정제표, Fibonacci SP, 작업 분해와 DoR·DoD로 반영 |
| 추가 PDF | MSA는 Bounded Context, API 우선, 데이터 소유권, 동기·비동기 통신과 실패 설계가 필요하다. | 서비스 경계·API 계약·ACL·데이터 일관성·미구현 복원력 항목을 구분해 기록 |

## 문서 해석 원칙

- PDF 안의 온라인 강의 플랫폼, 기업 사례, 인증·Eureka·Kafka 스프린트 구성과 시간표는 **설명용 예시**로 본다.
- 실제 프로젝트 요구사항은 사용자가 정한 B2B 장비 관리 도메인, 2일·2스프린트, 기존 MSA 최소 수정 원칙을 우선한다.
- 예시의 산출물 형식은 GearHub에 맞게 활용하지만, 예시 기능을 그대로 구현해야 하는 요구사항으로 해석하지 않는다.
- 실제 회의 참석자, 팀 합의, 이해관계자 피드백과 화면 캡처는 확인되지 않은 값을 만들지 않고 `TEAM INPUT`으로 남긴다.

## 현재 상태 표기

- `DONE`: 현재 소스와 통합 검증에서 확인됨
- `READY`: 코드 또는 문서는 준비됐지만 새 이름으로 컨테이너 재기동 전
- `TEAM INPUT`: 팀원 이름, 실제 회고 의견처럼 팀이 직접 채워야 함
- `FUTURE`: 이틀 MVP 범위 밖의 아이디어

## 제출 전 최소 확인

- [ ] 팀원 이름과 실제 담당을 Sprint 문서에 입력
- [x] 새 서비스명으로 전체 Docker Compose 재빌드·재기동
- [x] 운영진 계정으로 Auth·Gateway 경유 그룹 및 AI 화면 재검증
- [ ] 학생 계정으로 대여·반납 발표 동선 최종 리허설
- [ ] 팀원 전원이 Swagger UI에서 담당 API를 최소 1회 실제 호출
- [ ] `docs/evidence/screenshots`에 요청 전·후 화면 저장
- [ ] Sprint Board의 최종 상태와 Done SP를 실제 팀 결과로 갱신
- [ ] 실제 Review 피드백과 Retro 의견으로 예시 문구 교체
- [ ] 발표 자료는 이 문서의 순서를 바탕으로 별도 제작
