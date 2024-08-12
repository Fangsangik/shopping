package searching_program.search_product.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import searching_program.search_product.service.MemberService;

import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
public class CustomLoginController {

    private final MemberService memberService;

    @GetMapping("/login")
    //@RequestParam(value = "error")는 URL의 쿼리 파라미터 중 error라는 이름의 파라미터를 메소드의 파라미터로 바인딩
    public String loginPage(@RequestParam(value = "error", required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("error", "로그인 실패. 사용자명이나 비밀번호를 확인하세요.");
        }
        return "login";
    }
}