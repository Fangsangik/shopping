package searching_program.search_product.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import searching_program.search_product.dto.MemberDto;
import searching_program.search_product.error.CustomError;
import searching_program.search_product.service.MemberService;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    /**
     * 모든 회원 조회 메서드
     * @return 모든 회원 정보를 담은 리스트
     */
    @GetMapping
    public ResponseEntity<List<MemberDto>> getAllMembers() {
        List<MemberDto> members = memberService.findAll();
        log.info("모든 회원 정보 조회: 총 {}명", members.size());
        return ResponseEntity.ok(members);
    }

    /**
     * 특정 ID의 회원 조회 메서드
     * @param id 회원 ID
     * @return 해당 회원의 정보
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getMemberById(@PathVariable Long id) {
        Optional<MemberDto> memberDtoOptional = memberService.findById(id);
        if (memberDtoOptional.isPresent()) {
            log.info("회원 정보 조회 성공: ID={}", id);
            return ResponseEntity.ok(memberDtoOptional.get());
        } else {
            log.warn("회원 정보 조회 실패: ID={} 존재하지 않음", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("회원 정보를 찾을 수 없습니다.");
        }
    }

    /**
     * 회원 정보 업데이트 메서드
     * @param id 회원 ID
     * @param memberDto 업데이트할 회원 DTO
     * @param bindingResult 유효성 검사 결과
     * @return 업데이트 결과 응답
     */
    @PostMapping("/{id}")
    public ResponseEntity<?> updateMember(@PathVariable Long id,
                                          @Valid @RequestBody MemberDto memberDto,
                                          BindingResult bindingResult) {
        if (!id.equals(memberDto.getId())) {
            log.error("ID 불일치: PathVariable ID={}와 DTO ID={}", id, memberDto.getId());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("ID가 일치하지 않습니다.");
        }

        if (bindingResult.hasErrors()) {
            log.debug("유효성 검사 실패: {}", bindingResult.getAllErrors());
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }

        try {
            MemberDto updatedMember = memberService.updateMember(id, memberDto);
            log.info("회원 업데이트 성공: ID={}", id);
            return ResponseEntity.ok(updatedMember);
        } catch (CustomError e) {
            log.error("회원 업데이트 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * 회원 삭제 메서드
     * @param id 회원 ID
     * @param password 입력된 비밀번호
     * @return 삭제 결과 응답
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMember(@PathVariable Long id, @RequestParam String password) {
        log.debug("회원 삭제 요청: ID={}, 입력된 비밀번호={}", id, password);

        try {
            memberService.deleteMember(id, password);
            log.info("회원 삭제 성공: ID={}", id);
            return ResponseEntity.noContent().build(); // 삭제 성공, No Content 반환
        } catch (CustomError e) {
            log.error("회원 삭제 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
