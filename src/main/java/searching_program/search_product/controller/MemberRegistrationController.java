package searching_program.search_product.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import searching_program.search_product.dto.MemberDto;
import searching_program.search_product.error.CustomError;
import searching_program.search_product.service.MemberService;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/login")
public class MemberRegistrationController {

    private final MemberService memberService;


    // 회원가입
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody MemberDto memberDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            log.warn("회원가입 유효성 검사 실패: {}", bindingResult.getAllErrors());
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }

        try {
            MemberDto createdMember = memberService.createMember(memberDto);
            log.info("회원가입 성공: userId={}", createdMember.getUserId());
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "회원가입 성공", "member", createdMember));
        } catch (CustomError e) {
            log.error("회원가입 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }
}