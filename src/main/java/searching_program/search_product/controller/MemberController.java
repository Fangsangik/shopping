package searching_program.search_product.controller;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import searching_program.search_product.domain.Member;
import searching_program.search_product.dto.MemberDto;
import searching_program.search_product.service.MemberService;

import javax.validation.Valid;
import java.util.Optional;

@Controller
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/{id}")
    public String getMemberById(
            @PathVariable Long id,
            Model model) {

        try {
            MemberDto memberDto = memberService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("아이디가 존재하지 않습니다."));

            model.addAttribute("memberDto", memberDto);
            return "memberDetail";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "memberList";
        }
    }

    @GetMapping("/update/{id}")
    public String showUpdateMemberForm(@PathVariable Long id, Model model) {
        MemberDto memberDto = memberService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("아이디가 존재하지 않습니다."));

        model.addAttribute("memberDto", memberDto);
        return "updateForm";
    }

    @PostMapping("/update/{id}")
    public String updateMember(
            @PathVariable Long id,
            @Valid @ModelAttribute MemberDto memberDto,
            BindingResult bindingResult,
            Model model) {

        // ID가 DTO와 일치하는지 확인
        if (id == null || !id.equals(memberDto.getId())) {
            model.addAttribute("error", "ID가 일치하지 않습니다.");
            return "updateForm";
        }

        if (bindingResult.hasErrors()) {
            return "updateForm";
        }

        try {
            memberService.updateMember(id, memberDto);
            return "redirect:/members/" + id;
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "updateForm";
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteMember(
            @PathVariable Long id,
            @RequestParam String password,
            Model model) {

        try {
            memberService.deleteMember(id, password);
            return "redirect:/members";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "deleteForm";
        }
    }

    @GetMapping("/success")
    public String showSuccessPage() {
        return "success";
    }
}