package searching_program.search_product.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import searching_program.search_product.dto.MemberDto;
import searching_program.search_product.service.LoginService;


import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Optional;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class CustomLoginController {

    private final LoginService loginService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String userId, @RequestParam String password, HttpServletRequest request) {
        Optional<MemberDto> memberDtoOptional = loginService.loginCheck(userId, password);

        if (memberDtoOptional.isPresent()) {
            loginService.createSession(request, memberDtoOptional.get());
            return ResponseEntity.ok("로그인 성공");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인 실패. 사용자명이나 비밀번호를 확인하세요.");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        loginService.logout(request);
        return ResponseEntity.ok("로그아웃 성공");
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody MemberDto memberDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            log.warn("회원가입 유효성 검사 실패: {}", bindingResult.getAllErrors());
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }

        boolean isRegistered = loginService.registerMember(memberDto);

        if (isRegistered) {
            return ResponseEntity.status(HttpStatus.CREATED).body("회원가입 성공");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("회원가입 실패: 이미 존재하는 회원입니다.");
        }
    }

    @GetMapping("/login-attempts")
    public ResponseEntity<?> checkLoginAttempts(@RequestParam Long memberId) {
        int attempts = loginService.getLoginAttempts(memberId); // Assuming this method exists
        return ResponseEntity.ok(attempts);
    }
}
