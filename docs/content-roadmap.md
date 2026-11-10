# 첫 10개 게시글 로드맵

현재 실무 환경을 설명하는 글부터 시작해 테스트와 Spring Boot 개선으로 확장합니다. 아래 검증은 각 글의 완료 조건이며 수행 결과가 아닙니다. 01번만 초안과 코드가 있고 02~10번은 계획 단계입니다.

| 순서 / 제목 | 대표 시리즈 / 태그 | 핵심 내용과 예제 | 게시 전 검증 |
| --- | --- | --- | --- |
| 01. Spring MVC에서 Ajax 요청 시 JSON 대신 JSP 화면이 반환되는 이유 | 실무 트러블슈팅 / Troubleshooting, Ajax, Spring MVC | DispatcherServlet, 반환값 처리, JSP resolver, ResponseBody와 JSON View 비교. [초안](../posts/001-spring-mvc-ajax-json-vs-jsp.md) · [예제](../04-troubleshooting/ajax-json-vs-jsp/README.md) | Java 8 빌드, 4개 응답의 헤더·본문·콜백 비교 |
| 02. @ResponseBody 없이 Ajax에 JSON 반환하기 | Java & Spring / Java, Spring, Spring MVC | 첫 예제 확장. 이름 기반 View와 직접 View 지정, 모델 직렬화 범위, resolver order | 설정 누락 재현, JSON 구조와 민감 필드 제외 확인 |
| 03. Ajax success와 error는 어떻게 구분될까? | JSP & Web / jQuery, HTTP, Ajax | 200 JSON, 200 HTML, 400 JSON, 빈 응답, 잘못된 JSON의 비교 표 | HTTP 상태와 textStatus를 별도 기록; 204 동작도 구분 |
| 04. Ajax 요청에서 redirect가 동작하지 않는 이유 | 실무 트러블슈팅 / Troubleshooting, Ajax, HTTP | XHR의 redirect 추적과 브라우저 최상위 화면 이동 차이, JSON에 이동 경로 전달 | 최종 URL과 응답 본문 확인, 허용된 로컬 경로만 이동 |
| 05. MyBatis 동적 SQL에서 WHERE와 TRIM 활용하기 | Database & MyBatis / MyBatis, SQL | 조건 0개·1개·여러 개 SQL, 선행 AND 제거와 prefixOverrides 공백 | 실제 바인딩 SQL과 Oracle 결과 비교 |
| 06. MyBatis foreach로 다중 삭제 구현하기 | Database & MyBatis / MyBatis, SQL, Oracle | 독립 샘플 테이블, List 바인딩, 빈 목록 차단, 트랜잭션 | 대상 건수·롤백·빈 입력 확인, Oracle 버전별 IN 제한 확인 |
| 07. Oracle 시퀀스와 ORA-01031 오류 분석 | 실무 트러블슈팅 / Oracle, SQL, Troubleshooting | 시퀀스 생성·참조 권한 차이, 스키마 한정, 최소 권한 | 분리된 실습 계정에서 정확한 오류·권한 변경 전후 기록 |
| 08. JSP에서 jQuery로 동적 테이블 생성하기 | JSP & Web / JSP, jQuery, HTTP | JSON 배열을 DOM으로 변환, 빈 결과, 이벤트 위임과 text 삽입 | 특수 문자·악성 HTML을 문자열로 표시하는지 확인 |
| 09. Controller-Service-DAO 계층의 역할 | Java & Spring / Java, Spring, Architecture | HTTP 변환·업무 규칙·데이터 접근 분리, 트랜잭션 경계 | Service 단위 테스트와 DAO 통합 검증 역할 구분 |
| 10. Java 8 레거시 프로젝트를 Spring Boot로 개선하기 | Architecture & DevOps / Spring Boot, Architecture, Git | 설정·의존성 목록화, 작은 기능 이관, 동작 동등성·롤백 전략 | 선택 Boot 버전의 Java 요구사항 확인 후 대상 JDK 결정, 기존/전환 API 계약 비교 |

## 작성 순서와 완료 조건

한 글씩 재현 → 공식 근거 확인 → 해결 대안 비교 → 실제 검증 → 민감 정보 점검 → 게시 순서로 진행합니다. 1~4번은 같은 HTTP/MVC 기반 예제를 확장하고, 5~7번은 독립 DB 실습 환경을 명시합니다. 8~9번은 화면과 계층 책임을 정리한 뒤 10번에서 개선 방향을 연결합니다.

Spring Boot 전환 글에서도 Java 8 호환이 필요한 예제와 JDK 업그레이드가 필요한 후속 작업을 명확히 나눕니다. 최신 Boot가 Java 8을 지원한다고 가정하지 않습니다. 현재 저장소의 실행 코드는 Java 8 대상만 포함합니다.

개발 성장 기록은 각 단계 종료 시 별도 회고로 추가합니다. 배운 점, 실패한 가설, 검증한 AI 제안, 다음 실험을 기록하며 최초 10개 글의 순서를 변경하지 않습니다.

## 관리할 지표

게시일, Velog URL, 코드 경로, 정확한 실행 환경, 실제 검증 결과, 수정 이력을 글별로 남깁니다. 조회수만으로 성과를 판단하지 않고 재현 가능성, 원인 설명, 테스트와 개선 근거를 우선합니다.
