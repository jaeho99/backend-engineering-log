# Backend Engineering Log

**재호의 백엔드 개발 기록**

> 실무에서 마주한 문제를 분석하고, 해결 과정을 기록합니다.

Java/Spring 백엔드 개발자의 문제 해결과 성장 기록입니다. 현상을 재현하고 원인을 설명한 뒤 검증 가능한 예제로 해결 과정을 남깁니다. Velog에는 기술 설명을, GitHub에는 원문과 독립적인 샘플 코드를 관리해 취업 포트폴리오로 발전시킵니다.

## 기술 환경

현재: Java 8 · Spring MVC · eGovFramework · JSP/JSTL · JavaScript/jQuery/Ajax · MyBatis · Oracle SQL · Tomcat · Git

확장: Spring Boot · Spring Security · JPA/Hibernate · REST API · JUnit/Mockito · Docker · CI/CD · 시스템 설계 · 성능 최적화

## 기술 문서

| 카테고리 | 다루는 내용 |
| --- | --- |
| [Java & Spring](01-java-spring/README.md) | Java 8, MVC, 계층 설계, Spring Boot |
| [Database & MyBatis](02-database-mybatis/README.md) | Oracle, 동적 SQL, 트랜잭션, 최적화 |
| [JSP & Web](03-jsp-ajax/README.md) | JSP, JSTL, jQuery, Ajax, HTTP |
| [실무 트러블슈팅](04-troubleshooting/README.md) | 재현, 원인 분석, 해결, 검증 |
| [Architecture & DevOps](05-architecture-devops/README.md) | Git, 테스트, Jenkins, 배포, 설계 |
| [개발 성장 기록](06-growth/README.md) | 학습, AI 활용, 리팩터링, 회고 |

## 최신 게시글

| 상태 | 글 | 예제 |
| --- | --- | --- |
| 초안 · Velog 미게시 | [Spring MVC에서 Ajax 요청 시 JSON 대신 JSP 화면이 반환되는 이유](posts/001-spring-mvc-ajax-json-vs-jsp.md) | [최소 재현 프로젝트](04-troubleshooting/ajax-json-vs-jsp/README.md) |

Velog: [재호의 백엔드 개발 기록](https://velog.io/@dev_resolution)

프로필 소개: Java/Spring 백엔드 개발자 | 문제 해결과 성장의 기록

## 예제 이용 방법

예제별 README에서 JDK, 서버, 의존성과 실행 명령을 확인합니다. 첫 예제는 Java 8 문법의 Maven WAR이며 데이터베이스 없이 실행합니다. [실행 및 검증 절차](04-troubleshooting/ajax-json-vs-jsp/README.md)를 따라 예상 결과와 실제 결과를 비교하세요. 현재 빌드·브라우저 검증은 미실행입니다.

## 성장 방향

레거시 환경의 HTTP·MVC·SQL 동작을 먼저 이해하고 테스트를 추가합니다. 이후 Spring Boot 전환, 보안과 영속성, 배포 자동화, 성능 측정으로 범위를 넓힙니다. 측정하지 않은 성능 개선이나 수행하지 않은 테스트를 성과로 기록하지 않습니다.

- [첫 10개 글 로드맵](docs/content-roadmap.md)
- [게시 및 연결 가이드](docs/publishing-guide.md)
- [글 목록과 작성 규칙](posts/README.md)
- [공통 템플릿](posts/templates/post-template.md)
- [브랜드와 시각 자료](assets/README.md)

이 저장소의 예제는 회사 코드나 실제 데이터를 포함하지 않는 독립 샘플입니다. 공개 라이선스는 아직 지정하지 않았습니다. 외부 자료를 재사용할 때는 원저작자의 라이선스를 확인합니다.
