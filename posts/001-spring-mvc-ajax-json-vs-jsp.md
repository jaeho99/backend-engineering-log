# Spring MVC에서 Ajax 요청 시 JSON 대신 JSP 화면이 반환되는 이유

Ajax 요청에 `dataType: 'json'`을 설정했는데 응답이 HTML이고 `parsererror`가 발생한다면, 서버의 반환값이 어떤 경로로 처리되는지 확인해야 합니다. 이 글은 JSP 뷰 이름을 반환하는 코드를 재현하고, `@ResponseBody` 또는 JSON View로 응답을 만드는 방법을 비교합니다.

- 상태: 초안, 실행 검증 전
- 대표 시리즈: 01. 실무 트러블슈팅
- 태그: Troubleshooting, Ajax, Spring MVC
- 개발 환경: Java 8 대상 / Spring MVC 5.3.39 / JSP / jQuery 3.7.1 / Tomcat 9
- eGovFramework 및 MyBatis: 샘플 미포함, 기존 프로젝트 적용 시 버전·설정 별도 확인
- Velog: TODO — 게시 후 URL 추가

## 1. 문제 상황

클라이언트는 JSON 객체의 `message`를 기대하지만 서버는 JSP를 렌더링한 HTML을 반환합니다. HTTP 상태가 200이어도 JSON 해석에 실패하면 jQuery의 실패 콜백이 실행될 수 있습니다. 실제 환경에서는 로그인 페이지로의 리다이렉트나 공통 오류 페이지도 유사한 증상을 만들 수 있으므로 먼저 응답 본문을 확인합니다.

## 2. 문제 재현

[예제 실행 안내](../04-troubleshooting/ajax-json-vs-jsp/README.md)에 따라 WAR를 실행한 뒤 첫 버튼을 누릅니다. 오류를 만드는 서버 코드는 다음과 같습니다.

```java
@Controller
public class DemoController {
    @GetMapping("/demo/broken")
    public String broken(Model model) {
        model.addAttribute("message", "hello");
        return "result";
    }
}
```

```javascript
$.ajax({
    url: '/ajax-json-vs-jsp/demo/broken',
    method: 'GET',
    dataType: 'json'
}).done(function (data) {
    console.log(data.message);
}).fail(function (xhr, textStatus, errorThrown) {
    console.log(xhr.status, textStatus, errorThrown, xhr.responseText);
});
```

예상 결과는 HTTP 200, HTML 본문, `textStatus`의 `parsererror`입니다. 현재 이 결과를 실행 관측한 것은 아닙니다. 상세 오류 문자열은 브라우저와 jQuery 버전에 따라 달라질 수 있습니다.

## 3. 원인 분석

### Spring MVC 요청 처리 흐름

요청은 `DispatcherServlet`으로 들어옵니다. `HandlerMapping`이 컨트롤러 메서드를 찾고 `HandlerAdapter`가 실행합니다. 이후 반환값 처리기가 선언된 반환 타입과 애너테이션 등에 따라 응답 본문을 쓰거나 모델과 뷰를 준비합니다. 뷰 이름이면 `ViewResolver`가 실제 View를 찾고 View가 렌더링합니다. 모든 반환값이 ViewResolver를 거치는 것은 아닙니다.

```text
Ajax → DispatcherServlet → HandlerMapping → HandlerAdapter → Controller
  ├─ String 뷰 이름 → ViewResolver → JSP → HTML 응답
  ├─ @ResponseBody Map → HttpMessageConverter → JSON 응답
  └─ ModelAndView → JSON View 선택 → Jackson 직렬화 → JSON 응답
```

### @Controller와 @ResponseBody

`@Controller`는 MVC 컨트롤러를 표시합니다. 이것만으로 JSON 응답이 보장되지는 않습니다. 예제의 일반 `String` 반환값 `"result"`는 논리 뷰 이름입니다. `@ResponseBody`는 반환값을 HTTP 본문으로 작성하도록 지정하며 실제 표현 형식은 반환 타입과 메시지 컨버터, 콘텐츠 협상에 따라 결정됩니다. Map과 Jackson 컨버터가 있는 이 설정에서는 JSON으로 직렬화합니다. `@ResponseBody String`은 보통 문자열 본문을 쓰므로 애너테이션 자체가 모든 값을 JSON 객체로 바꾸는 것은 아닙니다.

### ViewResolver와 JSP 렌더링

예제의 `InternalResourceViewResolver`는 prefix `/WEB-INF/views/`와 suffix `.jsp`를 붙여 `result`를 `/WEB-INF/views/result.jsp`로 해석합니다. JSP 엔진이 생성하는 응답은 HTML입니다. 모델에 데이터를 추가한 것만으로 응답이 JSON이 되지는 않습니다.

### dataType, contentType, Content-Type

| 항목 | 지정 위치 | 의미 |
| --- | --- | --- |
| `dataType: 'json'` | jQuery 옵션 | 기대하는 응답 형식과 파싱 방식 |
| `contentType` | jQuery 옵션 | 서버로 보내는 요청 본문의 미디어 타입 |
| `Content-Type` 응답 헤더 | 서버 | 서버가 보낸 응답 표현의 미디어 타입 |

`dataType`은 Accept 헤더에도 영향을 주지만 서버를 강제로 JSON 생성기로 바꾸지는 않습니다. 이 샘플의 broken 메서드는 `produces` 제한이 없고 명시적인 JSP 뷰를 반환합니다. HTML을 JSON으로 해석하려 하면 파싱에 실패합니다. 헤더만 `application/json`으로 바꾸고 HTML을 보내는 것도 해결책이 아닙니다.

## 4. 해결 방법

### 방법 A: @ResponseBody와 Map

수정 전 `return "result";` 대신 데이터 객체를 반환하고 본문 처리 의도를 지정합니다.

```java
@GetMapping(value = "/demo/body", produces = "application/json")
@ResponseBody
public Map<String, Object> body() {
    Map<String, Object> data = new LinkedHashMap<String, Object>();
    data.put("message", "hello");
    return data;
}
```

예제는 `@EnableWebMvc`와 Jackson 의존성을 통해 JSON 메시지 컨버터를 사용합니다. 기존 XML 설정에서는 `<mvc:annotation-driven/>`와 컨버터 구성을 확인합니다. `produces`만 추가해도 뷰가 자동으로 JSON으로 바뀌는 것은 아닙니다.

### 방법 B: MappingJackson2JsonView와 이름 기반 ModelAndView

`MappingJackson2JsonView`는 모델 데이터를 Jackson으로 직렬화하는 View입니다. 아래 메서드는 `@ResponseBody` 없이 JSON View를 선택합니다.

```java
@GetMapping("/demo/named-view")
public ModelAndView namedView() {
    return new ModelAndView("jsonView", payload());
}
```

`payload()`는 `message=hello`인 Map을 만듭니다. 필요한 빈과 해석 순서는 다음과 같습니다. 전체 설정은 [WebConfig.java](../04-troubleshooting/ajax-json-vs-jsp/src/main/java/dev/backendlog/WebConfig.java)에 있습니다.

```java
@Bean
public MappingJackson2JsonView jsonView() {
    return new MappingJackson2JsonView();
}

@Bean
public BeanNameViewResolver beanNameViewResolver() {
    BeanNameViewResolver resolver = new BeanNameViewResolver();
    resolver.setOrder(0);
    return resolver;
}
// InternalResourceViewResolver는 order=10으로 뒤에 배치한다.
```

`BeanNameViewResolver`가 이름이 `jsonView`인 View 빈을 찾습니다. JSP resolver는 존재 여부와 무관하게 뷰 이름을 처리할 수 있어 마지막에 둡니다. JSON View는 기본적으로 모델의 항목들을 객체 속성으로 직렬화하므로 민감 데이터나 화면용 모델을 섞지 않습니다.

### 방법 C: ModelAndView에 View 객체 직접 전달

```java
@GetMapping("/demo/direct-view")
public ModelAndView directView() {
    return new ModelAndView(new MappingJackson2JsonView(), payload());
}
```

이 방식은 뷰 이름을 해석하지 않고 지정한 View로 렌더링합니다. 단순한 비교를 위해 View를 직접 생성했지만 공통 ObjectMapper 정책이 있다면 설정된 View 빈을 재사용하는 편이 일관성을 유지하기 쉽습니다. `ModelAndView` 자체가 JSON 반환을 뜻하는 것은 아닙니다. `new ModelAndView("result")`는 여전히 JSP 경로로 갈 수 있습니다.

### eGovFramework 적용 시 확인할 사항

이 샘플은 특정 전자정부프레임워크 버전의 기본 구성을 재현하지 않습니다. 실제 프로젝트의 `pom.xml`, DispatcherServlet URL 패턴(`*.do` 등), servlet context XML, ViewResolver 빈과 order, Jackson 의존성·컨버터, 인증 필터를 확인합니다. 이미 `jsonView`가 등록되어 있다면 중복 등록하지 않습니다. 이 Java Config를 기존 XML에 통째로 추가하면 핸들러·컨버터 구성이 중복될 수 있으므로 필요한 설정만 비교해 반영합니다.

## 5. 검증 방법

1. 개발자 도구 Network 탭을 열고 로그를 비운 뒤 각 버튼을 누릅니다.
2. 요청 URL과 상태, 리다이렉트 여부를 확인합니다.
3. Response Headers의 Content-Type과 Response의 실제 본문을 함께 확인합니다.
4. broken은 HTML과 parsererror, 나머지는 JSON과 done이 나오는지 비교합니다.
5. `mvn clean verify`로 뷰 forward 경로와 JSON 타입·구조를 확인합니다.

MockMvc는 JSP를 실제 렌더링하지 않습니다. 실제 HTML·브라우저 콜백 검증은 Tomcat에서 별도로 수행합니다. 현재 Maven 부재로 빌드·테스트·브라우저 검증은 미실행이며 [예제 README의 검증 표](../04-troubleshooting/ajax-json-vs-jsp/README.md)를 실행 후 채워야 합니다.

## 6. 정리

문제의 핵심은 클라이언트의 기대 형식과 서버의 반환 경로가 일치하지 않는 것입니다. 본문 응답에는 `@ResponseBody`와 컨버터를, 뷰 기반 JSON 응답에는 명시적인 JSON View를 사용합니다. 재발을 막으려면 상태 코드만 보지 않고 미디어 타입과 본문 구조까지 검증합니다.

## 참고 자료

- [Spring MVC — 반환값, ResponseBody, View 해석](https://docs.spring.io/spring-framework/docs/5.3.x/reference/html/web.html#mvc)
- [MappingJackson2JsonView API](https://docs.spring.io/spring-framework/docs/5.3.39/javadoc-api/org/springframework/web/servlet/view/json/MappingJackson2JsonView.html)
- [jQuery.ajax — dataType과 오류 콜백](https://api.jquery.com/jQuery.ajax/)
- [Spring 5.3.39 — Java 및 Servlet 요구사항](https://docs.spring.io/spring-framework/docs/5.3.39/reference/html/overview.html)

## GitHub 예제 코드

- [최소 재현 프로젝트 및 테스트 방법](../04-troubleshooting/ajax-json-vs-jsp/README.md)
- Velog용 공개 GitHub URL: https://github.com/jaeho99/backend-engineering-log/tree/main/04-troubleshooting/ajax-json-vs-jsp

## 관련 글

후속 글: [로드맵 02 — @ResponseBody 없이 Ajax에 JSON 반환하기](../docs/content-roadmap.md). 아직 미작성입니다.
