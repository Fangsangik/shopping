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
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping
    public String getAllMembers(Model model) {
        List<MemberDto> members = memberService.findAll();
        model.addAttribute("members", members);
        return "memberList"; // memberList.html 템플릿이 존재해야 함
    }

    @GetMapping("/{id}")
    public String getMemberById(@PathVariable Long id, Model model) {
        Optional<MemberDto> memberDtoOptional = memberService.findById(id);
        if (memberDtoOptional.isPresent()) {
            model.addAttribute("memberDto", memberDtoOptional.get());
        } else {
            model.addAttribute("error", "회원 정보를 찾을 수 없습니다.");
        }
        return "memberDetail";
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

        if (!id.equals(memberDto.getId())) {
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

    @DeleteMapping("/delete/{id}")
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
