package searching_program.search_product.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searching_program.search_product.domain.Member;
import searching_program.search_product.dto.DtoEntityConverter;
import searching_program.search_product.dto.MemberDto;
import searching_program.search_product.repository.MemberRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginService {

    private final MemberService memberService;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final DtoEntityConverter converter;
    private final Map<Long, Integer> loginAttempts = new HashMap<>();

    private String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    @Transactional
    public boolean registerMember(MemberDto memberDto) {
        if (validateMember(memberDto)) {
            return false;
        }

        memberDto.setPassword(encodePassword(memberDto.getPassword()));
        Member member = converter.convertToMemberEntity(memberDto);
        memberRepository.save(member);
        return true;
    }

    @Transactional
    private boolean validateMember(MemberDto memberDto) {
        if (memberDto.getUserId() == null) {
            throw new IllegalArgumentException("userId 값이 null이면 안됩니다");
        }

        if (memberDto.getPassword() == null) {
            throw new IllegalArgumentException("password 값이 null이면 안됩니다.");
        }

        Optional<Member> findMember = memberRepository.findByUserId(memberDto.getUserId());
        return findMember.isPresent();
    }

    @Transactional
    public Optional<MemberDto> loginCheck(String userId, String password) {
        try {
            MemberDto findMember = memberService.findByUserId(userId)
                    .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

            boolean isPasswordMatch = passwordEncoder.matches(password, findMember.getPassword());
            log.info("입력된 비밀번호: {}, 해시된 비밀번호: {}, 비교 결과: {}",
                    password, findMember.getPassword(), isPasswordMatch);

            if (isPasswordMatch) {
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

    @Transactional
    private void incrementLoginAttempts(Long memberId) {
        int attempts = loginAttempts.getOrDefault(memberId, 0);
        loginAttempts.put(memberId, attempts + 1);
    }

    public int getLoginAttempts(Long memberId) {
        return loginAttempts.getOrDefault(memberId, 0);
    }

    public void createSession(HttpServletRequest request, MemberDto memberDto) {
        HttpSession session = request.getSession();
        session.setMaxInactiveInterval(1800); // 세션 만료 시간
        session.setAttribute("member", memberDto.getId());
    }

    public Optional<MemberDto> loginTryCountLimit(MemberDto memberDto) {
        Long memberId = memberDto.getId();
        int attempts = loginAttempts.getOrDefault(memberId, 0);

        if (attempts >= 5) {
            memberService.lockMember(memberId);
            throw new IllegalArgumentException("계정이 잠겼습니다.");
        }

        try {
            MemberDto findMember = memberService.findById(memberId)
                    .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

            if (passwordEncoder.matches(memberDto.getPassword(), findMember.getPassword())) {
                loginAttempts.remove(memberId);
                return Optional.of(findMember);
            } else {
                loginAttempts.put(memberId, attempts + 1);
                return Optional.empty();
            }
        } catch (IllegalArgumentException e) {
            loginAttempts.put(memberId, attempts + 1);
            return Optional.empty();
        }
    }

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

