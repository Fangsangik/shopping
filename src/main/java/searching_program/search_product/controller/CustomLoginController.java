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
    private final MemberService memberService;

    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@RequestParam String userId,
                                   @RequestParam String password,
                                   HttpServletRequest request) {
        if (userId == null || password == null) {
            log.warn("로그인 요청 실패: userId 또는 password가 제공되지 않음");
            return ResponseEntity.badRequest().body("userId와 password는 필수 입력값입니다.");
        }

        Optional<MemberDto> memberDtoOptional = loginService.loginCheck(userId, password);

        if (memberDtoOptional.isPresent()) {
            loginService.createSession(request, memberDtoOptional.get());
            log.info("로그인 성공: userId={}", userId);
            return ResponseEntity.ok("로그인 성공");
        } else {
            log.warn("로그인 실패: userId={}", userId);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인 실패. 사용자명이나 비밀번호를 확인하세요.");
        }
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        loginService.logout(request);
        log.info("로그아웃 성공");
        return ResponseEntity.ok("로그아웃 성공");
    }

    @GetMapping("/login-attempts")
    public ResponseEntity<?> checkLoginAttempts(@RequestParam Long memberId) {
        int attempts = loginService.getLoginAttempts(memberId);
        log.info("로그인 시도 횟수 조회: memberId={}, attempts={}", memberId, attempts);
        return ResponseEntity.ok(attempts);
    }
}
