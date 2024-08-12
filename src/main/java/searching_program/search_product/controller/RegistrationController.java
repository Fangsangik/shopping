package searching_program.search_product.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import searching_program.search_product.dto.MemberDto;
import searching_program.search_product.service.MemberService;

import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
public class RegistrationController {

    private final MemberService memberService;

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("memberDto", new MemberDto());
        return "register";
    }

    @PostMapping("/register")
    public String registerMember(
            @Valid @ModelAttribute MemberDto memberDto,
            BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()) {
            return "register";
        }

        try {

            memberService.createMember(memberDto);
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }
}
