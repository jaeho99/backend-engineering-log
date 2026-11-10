# Ajax JSON/JSP 최소 재현 예제

[게시글 원문](../../posts/001-spring-mvc-ajax-json-vs-jsp.md) · [트러블슈팅 목록](../README.md)

## 목적과 환경

같은 데이터가 JSP와 JSON으로 반환되는 차이를 비교합니다. 회사 코드, DB, MyBatis, eGovFramework 의존성은 없습니다. eGovFramework 프로젝트에 적용할 때는 해당 프로젝트의 Spring 버전과 기존 MVC 설정을 별도로 확인해야 합니다.

| 구성 | 고정 기준 / 역할 |
| --- | --- |
| JDK | Java 8, source/target 1.8 |
| 빌드 | Maven 3.8.8 기준, WAR 패키징 |
| Spring MVC / Test | 5.3.39, 요청 처리 및 MockMvc |
| Jackson Databind | 2.17.2, JSON 직렬화 |
| Servlet API | javax.servlet 4.0.1, provided |
| 서버 | Tomcat 9.0.x의 JSP 엔진 사용, 구체적 패치 버전은 실행 시 기록 |
| jQuery | 3.7.1, WebJar로 제공하여 브라우저 CDN 접근 불필요 |
| JUnit | 4.13.2, 테스트용 |

이 버전들은 재현을 위해 선택한 고정값이며 사용자의 실무 버전이나 최신 권장 버전을 뜻하지 않습니다. Spring 5.3은 Java 8 및 javax.servlet 계열입니다. Tomcat 10+의 jakarta.servlet 환경에 그대로 배포하지 않습니다. 오래된 스택의 학습 예제이므로 외부 서비스 배포 전 지원 상태와 의존성 보안을 별도로 검토합니다. JSTL을 사용하지 않아 JSTL 의존성은 없습니다.

버전 근거: [Spring 5.3.39 개요](https://docs.spring.io/spring-framework/docs/5.3.39/reference/html/overview.html), [Jackson 2.17 릴리스](https://github.com/FasterXML/jackson/wiki/Jackson-Release-2.17), [jQuery 3.7.1 릴리스](https://blog.jquery.com/2023/08/28/jquery-3-7-1-released-reliable-table-row-dimensions/). 전체 조합의 실행 호환성은 아직 검증하지 않았습니다.

## 빌드와 실행

JDK 8과 Maven을 설치하고 `JAVA_HOME`을 JDK 8로 지정합니다. 최초 빌드는 Maven Central 접근이 필요합니다. 이 README가 있는 디렉터리에서 실행합니다.

```powershell
java -version
mvn -version
mvn clean verify
```

`target/ajax-json-vs-jsp.war`를 로컬 Tomcat 9의 `webapps`에 복사하고 서버를 시작합니다. 기존 동일 이름 앱을 덮어쓰기 전 별도로 확인하세요.

```powershell
# 본인 설치 경로로 변경합니다.
$tomcatPath = 'C:\tools\apache-tomcat-9'
Copy-Item -LiteralPath '.\target\ajax-json-vs-jsp.war' -Destination (Join-Path $tomcatPath 'webapps')
& (Join-Path $tomcatPath 'bin\startup.bat')
```

기본 포트 기준 브라우저에서 `http://localhost:8080/ajax-json-vs-jsp/`를 엽니다. 포트나 WAR 이름을 바꿨다면 URL도 수정합니다. 서버 로그는 해당 Tomcat의 `logs`에서 확인합니다.

## 검증 표

모든 버튼은 `dataType: 'json'`으로 GET 요청합니다.

| 경로 (`/demo/` 아래) | 예상 상태 | 예상 Content-Type / 본문 | 예상 콜백 |
| --- | --- | --- | --- |
| broken | 200 | text/html / JSP가 렌더링한 HTML | fail, parsererror |
| body | 200 | application/json / `{"message":"hello"}` | done |
| named-view | 200 | application/json / 같은 JSON 객체 | done |
| direct-view | 200 | application/json / 같은 JSON 객체 | done |

Network 탭에서 요청 URL·상태·Response Headers·Response 본문을 기록합니다. `broken`은 HTTP 성공이어도 JSON 파싱 실패입니다. 오류 메시지의 상세 문자열은 브라우저마다 다를 수 있습니다.

```powershell
curl.exe -i -H 'Accept: application/json' http://localhost:8080/ajax-json-vs-jsp/demo/broken
curl.exe -i http://localhost:8080/ajax-json-vs-jsp/demo/body
curl.exe -i http://localhost:8080/ajax-json-vs-jsp/demo/named-view
curl.exe -i http://localhost:8080/ajax-json-vs-jsp/demo/direct-view
```

MockMvc 테스트는 실제 설정을 로딩해 JSP forward 경로와 JSON의 타입·구조를 검증합니다. JSP 엔진을 실행하지 않으므로 실제 HTML 렌더링이나 jQuery parsererror는 Tomcat/브라우저로 추가 검증해야 합니다.

## 현재 검증 상태

- 작성 환경: Java 17.0.15 확인, Maven 명령 없음.
- Java 8 컴파일 / Maven 테스트 / WAR 패키징: 미실행.
- Tomcat 배포 / JSP 렌더링 / 브라우저 Network 검증: 미실행.
- 위 표는 예상 결과이며 관측한 결과가 아닙니다.
- 게시 전 실제 JDK·Maven·Tomcat 패치 버전, 명령, 실행일, 테스트 결과와 캡처를 여기에 추가합니다.

## 문제 해결

- 404: context path, WAR 배포 로그와 DispatcherServlet 매핑을 확인합니다.
- `jsonView.jsp`를 찾는 오류: BeanNameViewResolver 등록과 우선순위를 확인합니다.
- JSON 변환 실패: Jackson 의존성, MVC 메시지 컨버터와 반환 객체를 확인합니다.
- jQuery 미로딩: Network에서 WebJar JS 요청의 상태와 정적 리소스 설정을 확인합니다.
- 로그인 HTML 반환: 인증 필터의 redirect와 최종 응답 URL을 확인합니다. 이 샘플에는 인증 기능이 없습니다.
