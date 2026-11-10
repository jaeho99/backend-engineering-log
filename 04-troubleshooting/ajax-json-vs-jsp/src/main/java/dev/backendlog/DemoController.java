package dev.backendlog;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

@Controller
public class DemoController {
    @GetMapping("/")
    public String index() {
        return "index";
    }

    // 오류 재현: 논리 뷰 이름이므로 JSP 렌더링 경로로 이동한다.
    @GetMapping("/demo/broken")
    public String broken(Model model) {
        model.addAttribute("message", "hello");
        return "result";
    }

    @GetMapping(value = "/demo/body", produces = "application/json")
    @ResponseBody
    public Map<String, Object> body() {
        return payload();
    }

    // 이름으로 View 빈을 찾는 방식. BeanNameViewResolver 설정이 필요하다.
    @GetMapping("/demo/named-view")
    public ModelAndView namedView() {
        return new ModelAndView("jsonView", payload());
    }

    // View 객체를 직접 지정하므로 이름 기반 ViewResolver를 거치지 않는다.
    @GetMapping("/demo/direct-view")
    public ModelAndView directView() {
        return new ModelAndView(new MappingJackson2JsonView(), payload());
    }

    private Map<String, Object> payload() {
        Map<String, Object> data = new LinkedHashMap<String, Object>();
        data.put("message", "hello");
        return data;
    }
}
