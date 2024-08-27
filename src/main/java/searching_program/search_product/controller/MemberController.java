package searching_program.search_product.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
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

    @GetMapping
    public ResponseEntity<List<MemberDto>> getAllMembers() {
        List<MemberDto> members = memberService.findAll();
        return ResponseEntity.ok(members);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getMemberById(@PathVariable Long id) {
        Optional<MemberDto> memberDtoOptional = memberService.findById(id);
        if (memberDtoOptional.isPresent()) {
            return ResponseEntity.ok(memberDtoOptional.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("회원 정보를 찾을 수 없습니다.");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateMember(@PathVariable Long id,
                                          @Valid @RequestBody MemberDto memberDto,
                                          BindingResult bindingResult) {
        if (!id.equals(memberDto.getId())) {
            log.error("ID가 일치하지 않음: PathVariable ID={}와 DTO ID={}", id, memberDto.getId());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("ID가 일치하지 않습니다.");
        }

        if (bindingResult.hasErrors()) {
            log.debug("유효성 검사 실패: {}", bindingResult.getAllErrors());
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }

        try {
            memberService.updateMember(id, memberDto);
            log.info("회원 업데이트 성공: ID={}", id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            log.error("회원 업데이트 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMember(@PathVariable Long id, @RequestParam String password) {
        log.debug("회원 삭제 요청: ID={}, 입력된 비밀번호={}", id, password);

        try {
            memberService.deleteMember(id, password);
            log.info("회원 삭제 성공: ID={}", id);
            return ResponseEntity.noContent().build(); // 삭제 성공, No Content 반환
        } catch (IllegalArgumentException e) {
            log.error("회원 삭제 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
