# 화면 증빙 저장 위치

가이드에서 요구하는 “요청 전·후 상태 변화가 드러나는 동작 화면”을 이 디렉터리에 저장한다.

필수 캡처 이름과 내용은 [07_VALIDATION_AND_DEMO.md](../../agile/07_VALIDATION_AND_DEMO.md)의 `화면 캡처 목록`을 따른다.

## 주의사항

- Access Token, 비밀번호와 Client Secret을 캡처하지 않는다.
- 새 서비스명으로 Docker를 재기동한 뒤 Eureka 화면을 캡처한다.
- 대여 전·후 재고가 같은 Asset ID에서 변경되었는지 확인한다.
- 구매 승인·반려는 서로 다른 Request를 사용한다.
