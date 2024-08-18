package searching_program.search_product.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import searching_program.search_product.dto.MemberDto;
import searching_program.search_product.service.MemberService;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@Slf4j
@Controller
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/members")
    public String listMembers(Model model) {
        List<MemberDto> members = memberService.findAllMembers();
        model.addAttribute("members", members);
        return "memberList";
    }

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

    @GetMapping("/delete/{id}")
    public String getDeleteForm(@PathVariable Long id, Model model) {
        log.debug("회원 삭제 폼 요청: ID={}", id);
        model.addAttribute("memberId", id);
        return "deleteForm";
    }

    @PostMapping("/delete/{id}")
    public String deleteMember(
            @PathVariable Long id,
            @RequestParam String password,
            Model model) {

        log.debug("회원 삭제 요청: ID={}, 입력된 비밀번호={}", id, password);

        try {
            memberService.deleteMember(id, password);
            log.info("회원 삭제 성공: ID={}", id);
            return "redirect:/members";
        } catch (IllegalArgumentException e) {
            log.error("회원 삭제 실패: {}", e.getMessage());
            model.addAttribute("error", e.getMessage());
            return "deleteForm";
        }
    }


    @PostMapping("/update/{id}")
    public String updateMember(
            @PathVariable Long id,
            @Valid @ModelAttribute MemberDto memberDto,  // @ModelAttribute로 바인딩된 객체
            BindingResult bindingResult,  // 모델 객체 바로 뒤에 선언되어야 함
            Model model) {

        log.debug("업데이트 요청: PathVariable ID={}, DTO ID={}", id, memberDto.getId());

        if (!id.equals(memberDto.getId())) {
            log.error("ID가 일치하지 않음: PathVariable ID={}와 DTO ID={}", id, memberDto.getId());
            model.addAttribute("error", "ID가 일치하지 않습니다.");
            return "updateForm";
        }

        if (bindingResult.hasErrors()) {
            log.debug("유효성 검사 실패: {}", bindingResult.getAllErrors());
            return "updateForm";
        }

        try {
            memberService.updateMember(id, memberDto);
            log.info("회원 업데이트 성공: ID={}", id);
            return "redirect:/members/";  // 전체 회원의 목록을 표시하는 페이지로 돌아가기
        } catch (IllegalArgumentException e) {
            log.error("회원 업데이트 실패: {}", e.getMessage());
            model.addAttribute("error", e.getMessage());
            return "updateForm";
        }
    }

    @GetMapping("/success")
    public String showSuccessPage() {
        return "success";
    }
}
