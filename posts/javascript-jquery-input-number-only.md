# [JavaScript/jQuery] input 숫자만 입력받기 — 정규식, 이벤트 위임, 한글 IME 처리

JSP 검색 화면에서 번호와 이름 검색에 같은 입력칸을 사용했습니다. 번호를 선택하면 숫자 여섯 자리만 받아야 했는데, `maxlength="6"`을 적용해도 영문과 한글이 들어갔습니다. 이 글에서는 JavaScript 정규식으로 입력값을 정리하고, jQuery 이벤트 위임과 한글 조합 처리를 추가한 뒤, 검색 요청을 보내기 직전에 여섯 자리를 확인하는 방법을 정리합니다.

- 개발 환경 · Java 8, JSP, jQuery, Spring MVC / 전자정부프레임워크, MyBatis, Oracle DB
- 대표 시리즈 · 실무 트러블슈팅
- 태그 · JavaScript, jQuery, JSP, 정규식, input, 유효성검사, 이벤트위임, IME, 트러블슈팅

## 1. 문제 상황 — 공통 검색 입력칸에 조건마다 다른 규칙이 필요했다

검색 조건이 바뀌어도 화면에서 같은 `.searchVal` 입력칸을 사용했습니다. `searchType === "6"`이면 번호 검색이므로 `0`부터 `9`까지 최대 여섯 자리를 입력하고, 검색할 때는 정확히 여섯 자리가 필요합니다. 이름 등 다른 조건에서는 한글과 영문을 입력할 수 있어야 합니다. 조건을 바꿀 때는 이전 검색어를 비웁니다.

`maxlength`는 텍스트 입력의 최대 길이를 제한하는 HTML 속성입니다. 길이는 UTF-16 코드 단위로 세며, 허용할 문자의 종류는 지정하지 않습니다. 그래서 `maxlength="6"`이어도 `abc`나 `가나다`를 입력할 수 있습니다. 이 동작은 `maxlength`가 맡는 범위로 설명됩니다. 화면 갱신 후 핸들러가 끊기는 문제나 한글 조합 중 입력이 깨지는 문제는 별도로 확인할 항목입니다. [WHATWG HTML 표준 — maxlength](https://html.spec.whatwg.org/multipage/form-control-infrastructure.html#attr-fe-maxlength)

## 2. JavaScript 정규식으로 숫자만 입력받기

```javascript
var cleaned = value.replace(/[^0-9]/g, '').slice(0, 6);
var valid = /^[0-9]{6}$/.test(value);
```

- `/[^0-9]/g`에서 `[^0-9]`는 ASCII 숫자 `0`~`9` 이외의 문자를 찾습니다. `g`를 붙여 첫 번째 일치에서 멈추지 않고 모두 찾습니다.
- `replace(/[^0-9]/g, '')`는 찾은 문자를 빈 문자열로 바꿉니다. 이어서 `slice(0, 6)`으로 앞쪽 숫자 여섯 자리까지만 남깁니다. 예를 들어 `ab00-1234한5`를 붙여넣으면 `001234`가 남습니다.
- `/^[0-9]{6}$/`의 `^`와 `$`는 문자열의 시작과 끝을, `{6}`은 정확히 여섯 번을 뜻합니다. 입력 중에는 `00123`도 유지하지만, 검색할 때 이 정규식으로 검사하면 `00123`은 거절됩니다.

이 번호는 앞의 `0`도 의미가 있는 식별자라서 `type="text"`와 문자열을 사용했습니다. 정리하는 순서도 중요합니다. HTML에 `maxlength="6"`을 두면 브라우저가 붙여넣은 원문을 먼저 잘라 필요한 숫자까지 빠질 수 있습니다. 예제에서는 숫자 외 문자를 제거한 다음 길이를 제한합니다. 전각 숫자 `１２３`도 이 규칙에서는 제거됩니다. [ECMAScript 표준 — String.prototype.replace](https://tc39.es/ecma262/#sec-string.prototype.replace)

## 3. jQuery 이벤트 위임으로 교체된 input 처리하기

```javascript
// 직접 등록
$('.searchVal').on('input', handleInput);

// 상위 요소에 위임
$('#searchArea').on('input', '.searchVal', handleInput);
```

직접 등록은 호출 시점에 선택된 input에 핸들러를 붙입니다. Ajax 응답으로 기존 input을 제거하고 새 input을 만들면, 클래스 이름이 같아도 새 DOM 요소에는 그 핸들러가 없습니다. 화면 갱신 뒤에만 숫자 제한이 풀린다면 먼저 input이 교체되는지 확인할 수 있습니다.

이벤트 위임은 계속 유지되는 `#searchArea`에 핸들러를 붙입니다. 하위 input에서 발생한 이벤트가 상위로 전파되면 jQuery가 `.searchVal`과 일치하는지 검사하므로 나중에 추가된 input도 처리합니다. `#searchArea`까지 교체한다면 그 바깥의 고정된 요소에 위임하거나 교체 후 초기화 코드를 다시 실행해야 합니다. [jQuery 공식 문서 — .on()](https://api.jquery.com/on/)

## 4. 한글 IME 조합이 끝난 뒤 입력값 정리하기

한글은 `ㅎ`, `하`, `한`처럼 조합 중에도 입력값이 바뀔 수 있습니다. 이때 `.value`를 다시 쓰면 브라우저가 관리하는 조합이나 커서 위치에 영향을 주어 입력이 끊기거나 글자가 사라질 수 있습니다. 번호 모드에서는 조합이 끝난 뒤 한글을 제거하고, 이름 모드에서는 조합 결과를 그대로 둡니다.

`compositionstart`에서 해당 input의 조합 상태를 기록하고, `compositionend`에서 상태를 지운 뒤 값을 정리합니다. `input`에서는 jQuery 이벤트가 감싸고 있는 원본 이벤트의 `event.originalEvent.isComposing`도 확인합니다. 이 속성의 지원 여부와 마지막 `input`이 발생하는 시점은 브라우저 및 IME 조합에 따라 확인이 필요합니다. 두 신호를 함께 사용하고, 조합 종료와 후속 `input` 양쪽에서 정리하더라도 값이 바뀔 때만 다시 씁니다. [W3C UI Events — Composition Events](https://www.w3.org/TR/uievents/#events-compositionevents) · [MDN — InputEvent.isComposing](https://developer.mozilla.org/en-US/docs/Web/API/InputEvent/isComposing)

## 5. 최종 구현 — JSP와 jQuery input 숫자 제한

JSP에서는 검색 폼을 위임 대상으로 사용합니다. 프로젝트에서 jQuery를 먼저 로드하고, `/search/items`는 실제 검색 경로에 맞춥니다. 검색 버튼과 Enter는 모두 폼의 `submit` 핸들러를 거칩니다.

```jsp
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:url var="searchUrl" value="/search/items" />

<form id="searchArea" action="${searchUrl}" method="get">
    <label for="searchType">검색 조건</label>
    <select id="searchType" name="searchType">
        <option value="1">이름</option>
        <option value="6">번호</option>
    </select>

    <label for="searchVal">검색어</label>
    <input id="searchVal" class="searchVal" name="searchVal"
           type="text" aria-describedby="searchMessage">
    <button type="submit">검색</button>
    <p id="searchMessage" role="status"></p>
</form>
```

```javascript
$(function () {
    var $area = $('#searchArea');
    var $message = $area.find('#searchMessage');
    var inputSelector = 'input.searchVal[name="searchVal"]';

    function getSearchType() {
        return $area.find('select[name="searchType"]').val();
    }

    function cleanNumber(input) {
        if (getSearchType() !== '6') {
            return;
        }

        var cleaned = input.value.replace(/[^0-9]/g, '').slice(0, 6);
        if (input.value !== cleaned) {
            input.value = cleaned;
        }
    }

    $area.on('compositionstart', inputSelector, function () {
        $(this).data('composing', true);
    });

    $area.on('compositionend', inputSelector, function () {
        $(this).removeData('composing');
        cleanNumber(this);
    });

    $area.on('input', inputSelector, function (event) {
        if ($(this).data('composing') ||
                (event.originalEvent && event.originalEvent.isComposing)) {
            return;
        }
        cleanNumber(this);
    });

    $area.on('change', 'select[name="searchType"]', function () {
        $area.find(inputSelector).val('').removeData('composing');
        $message.text('');
    });

    $area.on('submit', function (event) {
        event.preventDefault();
        var searchType = getSearchType();
        var $input = $area.find(inputSelector);

        if ($input.length !== 1) {
            $message.text('검색 입력칸을 확인해 주세요.');
            return;
        }
        if ($input.data('composing')) {
            $message.text('입력을 완료한 뒤 검색해 주세요.');
            return;
        }

        var searchVal = $input.val();
        if (searchType === '6' && !/^[0-9]{6}$/.test(searchVal)) {
            $message.text('번호는 숫자 6자리로 입력해 주세요.');
            $input.trigger('focus');
            return;
        }

        $message.text('');
        $.ajax({
            url: $area.attr('action'),
            type: 'GET',
            dataType: 'json',
            data: { searchType: searchType, searchVal: searchVal }
        }).done(function (response) {
            $message.text('검색 요청을 처리했습니다.');
            // response.items를 기존 결과 영역에 렌더링합니다.
        }).fail(function (xhr) {
            var body = xhr.responseJSON;
            $message.text(body && body.message ? body.message : '검색에 실패했습니다.');
        });
    });
});
```

입력 정리는 `keydown` 대신 값이 바뀐 뒤 발생하는 `input`에서 처리하므로 키보드 입력과 붙여넣기에 같은 규칙을 적용합니다. 코드에서 `.val(...)`로 직접 값을 넣으면 `input`이 자동으로 발생하지 않으므로, Ajax 직전에는 **현재 값을 다시 읽어 정리 없이 검증**합니다. 예를 들어 코드로 일곱 자리를 넣으면 검색 단계에서 거절됩니다. [MDN — input 이벤트](https://developer.mozilla.org/en-US/docs/Web/API/Element/input_event) · [jQuery 공식 문서 — .val()](https://api.jquery.com/val/)

이 예제는 폼마다 전송 대상 input이 하나라는 전제를 둡니다. 검색 폼이 여러 개라면 폼마다 고유한 ID를 쓰고 해당 폼을 기준으로 검색 조건과 입력값을 조회합니다. 같은 폼에 여러 `.searchVal`이 있다면 `name`이나 활성 상태 등으로 전송 대상을 명확하게 지정합니다. 여러 요소를 한 번에 선택한 뒤 `.val()`로 읽으면 첫 번째 요소의 값이 반환됩니다.

## 6. 검증 방법 — 예상 동작과 실행 결과

브라우저에서 번호 조건으로 시작해 아래 순서대로 확인합니다. 검색을 누르는 항목은 개발자 도구의 Network 탭에서 `/search/items` 요청과 파라미터를 확인합니다. input 교체는 폼을 유지한 채 동일한 `name`과 클래스를 가진 새 요소를 넣어 확인합니다.

| 테스트 | 기대 결과 | 실제 브라우저 검증 |
| --- | --- | --- |
| 숫자 6자리 `001234` 입력 | 앞의 `0`을 포함해 `001234` 유지 | 미실행 |
| 숫자 7자리 `1234567` 입력 | `123456`까지만 유지 | 미실행 |
| 영문 `abc` 입력 | 번호 모드에서는 제거, 이름 모드에서는 유지 | 미실행 |
| 한글 `한글` 입력 | 조합 중 값을 덮어쓰지 않으며, 조합 종료 후 번호 모드에서는 제거, 이름 모드에서는 유지 | 미실행 |
| 특수문자 `!@#` 입력 | 번호 모드에서 제거 | 미실행 |
| `ab00-1234한5` 붙여넣기 | `001234`만 남음 | 미실행 |
| 검색 조건 변경 | 기존 입력값과 안내 메시지 초기화 | 미실행 |
| `12345` 입력 후 검색 | 6자리 안내가 표시되고 Ajax 요청 없음 | 미실행 |
| `001234` 입력 후 검색 | `searchType=6`, `searchVal=001234`로 Ajax 요청 | 미실행 |
| 동적으로 input 교체 후 `a1234567` 입력 | 폼을 유지하면 새 input에도 적용되어 `123456`만 남음 | 미실행 |

브라우저와 별도로, 위 jQuery 코드 블록에서 핸들러를 추출해 Node.js 22에서 실행했습니다. jQuery 호출과 Ajax를 테스트용 객체로 대체해 입력 정리, 조합 상태에 따른 분기, 조건 변경 시 초기화, 5자리 요청 차단, `001234`의 전송 파라미터를 확인했습니다. Java 정규식은 JDK 17에서 `javac --release 8`로 컴파일한 뒤 `001234`를 통과시키고 5자리, 7자리, 문자 혼합, 앞 공백, 뒤 줄바꿈, 전각 숫자를 거절하는 것을 확인했습니다. 실제 브라우저의 이벤트 전파와 운영체제 IME, JSP 렌더링 및 Spring MVC·Oracle DB 연동은 이 검증 범위에 포함되지 않습니다.

실제 배포 대상 브라우저에서는 한글 조합 중 Enter를 눌러 글자를 확정하는 경우도 확인합니다. 조합이 활성화된 상태에서 `submit`이 발생하면 예제는 요청을 막습니다. 브라우저가 `submit`보다 먼저 조합을 종료하는 경우에는 이미 확정된 값으로 최종 검증이 실행됩니다.

## 7. Java 8과 Spring MVC에서 요청값 다시 검증하기

브라우저에서 값을 정리해도 호출자가 HTTP 요청을 직접 보내면 그 로직을 거치지 않습니다. Controller는 번호 조건에서 들어온 값을 문자열 그대로 검증합니다. 잘못된 요청을 서버가 임의로 고치면 원래와 다른 번호를 조회할 수 있으므로 HTTP `400 Bad Request`와 오류 메시지를 반환합니다.

다음 Controller는 예제의 이름(`1`)과 번호(`6`)만 허용합니다. 정상 응답의 빈 `items`는 서비스 연결 위치를 보여주기 위한 값입니다. 프로젝트에 적용할 때는 검증 뒤 실제 검색 서비스를 호출하고 JSON 메시지 컨버터가 설정되어 있는지 확인합니다.

```java
import java.util.Collections;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SearchController {
    private static final Pattern SEARCH_NUMBER = Pattern.compile("[0-9]{6}");

    @RequestMapping(value = "/search/items", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> search(
            @RequestParam(value = "searchType", required = false) String searchType,
            @RequestParam(value = "searchVal", required = false) String searchVal) {
        if (!"1".equals(searchType) && !"6".equals(searchType)) {
            return badRequest("지원하지 않는 검색 조건입니다.");
        }
        if (searchVal == null) {
            return badRequest("검색어를 입력해 주세요.");
        }
        if ("6".equals(searchType) && !SEARCH_NUMBER.matcher(searchVal).matches()) {
            return badRequest("번호는 숫자 6자리로 입력해 주세요.");
        }

        // 이 위치에서 검색 서비스를 호출합니다.
        return ResponseEntity.ok(Collections.<String, Object>singletonMap(
                "items", Collections.emptyList()));
    }

    private ResponseEntity<Map<String, Object>> badRequest(String message) {
        return ResponseEntity.badRequest().body(
                Collections.<String, Object>singletonMap("message", message));
    }
}
```

Java의 `Matcher.matches()`는 입력 전체를 검사하므로 패턴에 `^`와 `$`를 추가할 필요가 없습니다. 앞뒤 공백도 허용하지 않으며 `001234`는 문자열 그대로 전달합니다. Controller에서 먼저 검사하면 잘못된 요청에 일관된 HTTP 오류를 반환하고 불필요한 DB 조회를 피할 수 있습니다. MyBatis는 검증을 통과한 값을 `#{searchVal}`로 바인딩하고, 검색 조건에 맞는 SQL을 실행하는 역할을 맡습니다. [Oracle Java 8 API — Matcher.matches()](https://docs.oracle.com/javase/8/docs/api/java/util/regex/Matcher.html#matches--) · [MyBatis 공식 문서 — Mapper XML](https://mybatis.org/mybatis-3/sqlmap-xml.html#Parameters)

## 8. 실무에서 배운 점

- `maxlength`는 길이를 제한합니다. 허용할 문자와 검색 요청의 조건은 JavaScript와 서버에서 명시합니다.
- 입력칸이 교체될 수 있다면 고정된 상위 요소에 jQuery 이벤트 위임을 적용합니다.
- input 한글 입력 제한을 구현할 때는 조합 중인 값을 그대로 두고 확정된 값을 정리합니다.
- 입력 중에는 최대 여섯 자리로 정리하고, 검색 직전 JavaScript 유효성 검사에서는 정확히 여섯 자리를 요구합니다.
- 프론트엔드는 입력 중 피드백을 제공하고, 백엔드는 실제로 받은 요청이 조회 조건을 충족하는지 판단합니다.
