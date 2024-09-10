package searching_program.search_product.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searching_program.search_product.dto.MemberDto;
import searching_program.search_product.error.CustomError;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static searching_program.search_product.type.ErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginService {

    private final MemberService memberService;
    private final PasswordEncoder passwordEncoder;
    private final Map<Long, Integer> loginAttempts = new HashMap<>();

    /**
     * 로그인 검증 메서드
     * @param userId 사용자 ID
     * @param password 입력된 비밀번호
     * @return 로그인 성공 시 Optional<MemberDto>, 실패 시 Optional.empty()
     */
    @Transactional
    public Optional<MemberDto> loginCheck(String userId, String password) {
        try {
            MemberDto findMember = memberService.findByUserId(userId)
                    .orElseThrow(() -> new CustomError(USER_NOT_FOUND));

            // 계정이 잠겨있는지 확인
            if (findMember.isLock()) {
                log.warn("계정이 잠겼습니다. userId: {}", userId);
                throw new CustomError(ACCOUNT_IS_LOCKED); // 계정이 잠긴 경우 예외 던지기
            }

            boolean isPasswordMatch = passwordEncoder.matches(password, findMember.getPassword());
            log.info("입력된 비밀번호: {}, 해시된 비밀번호: {}, 비교 결과: {}",
                    password, findMember.getPassword(), isPasswordMatch);

            if (isPasswordMatch) {
                resetLoginAttempts(findMember.getId());
                return Optional.of(findMember);
            } else {
                log.info("로그인 실패 - userId : {} 비밀번호 불일치", userId);
                incrementLoginAttempts(findMember.getId());
                return Optional.empty();
            }
        } catch (IllegalArgumentException e) {
            log.info("로그인 실패 - userId : {} 존재하지 않음", userId);
            return Optional.empty();
        }
    }

    /**
     * 로그인 시도 횟수 증가 메서드
     * @param memberId 회원 ID
     */
    @Transactional
    protected void incrementLoginAttempts(Long memberId) {
        int attempts = loginAttempts.getOrDefault(memberId, 0);
        loginAttempts.put(memberId, attempts + 1);

        if (attempts + 1 >= 5) {
            memberService.lockMember(memberId);
            log.warn("계정이 잠겼습니다. memberId: {}", memberId);
            throw new CustomError(ACCOUNT_IS_LOCKED);
        }
    }

    /**
     * 로그인 시도 횟수 초기화 메서드
     * @param memberId 회원 ID
     */
    private void resetLoginAttempts(Long memberId) {
        loginAttempts.remove(memberId);
    }

    /**
     * 특정 회원의 로그인 시도 횟수 조회
     * @param memberId 회원 ID
     * @return 로그인 시도 횟수
     */
    public int getLoginAttempts(Long memberId) {
        return loginAttempts.getOrDefault(memberId, 0);
    }

    /**
     * 세션 생성 메서드
     * @param request HTTP 요청 객체
     * @param memberDto 회원 DTO
     */
    public void createSession(HttpServletRequest request, MemberDto memberDto) {
        HttpSession session = request.getSession();
        session.setMaxInactiveInterval(1800); // 세션 만료 시간 설정 (1800초 = 30분)
        session.setAttribute("member", memberDto.getId());
    }

    /**
     * 로그아웃 메서드
     * @param request HTTP 요청 객체
     */
    @Transactional
    public void logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            Long memberId = (Long) session.getAttribute("member");
            log.info("로그아웃 성공, memberId : {}", memberId);
            session.invalidate();
        }
    }
}



