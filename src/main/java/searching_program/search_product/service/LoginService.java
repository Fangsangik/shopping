package searching_program.search_product.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searching_program.search_product.domain.Member;
import searching_program.search_product.dto.MemberDto;
import searching_program.search_product.repository.MemberRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class LoginService {

    private final MemberService memberService;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private Map<Long, Integer> loginAttempts = new HashMap<>();

    // 비밀번호 해시화 메서드
    private String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    public boolean registerMember(MemberDto memberDto) {
        if (validateMember(memberDto)) {
            return false;
        }

        // 비밀번호 해시화
        memberDto.setPassword(encodePassword(memberDto.getPassword()));

        Member member = convertToEntity(memberDto);
        memberRepository.save(member);
        return true;
    }

    // 동일 아이디 존재 검사
    private boolean validateMember(MemberDto memberDto) {
        Optional<Member> findMember = memberRepository.findById(memberDto.getId());
        return findMember.isPresent();
    }

    public Optional<MemberDto> loginCheck(MemberDto memberDto) {
        try {
            // userId를 통해 회원 정보 조회
            MemberDto findMember = memberService.findByUserId(memberDto.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

            // 입력된 비밀번호와 저장된 해시된 비밀번호 비교
            boolean isPasswordMatch = passwordEncoder.matches(memberDto.getPassword(), findMember.getPassword());
            log.info("입력된 비밀번호: {}, 해시된 비밀번호: {}, 비교 결과: {}",
                    memberDto.getPassword(), findMember.getPassword(), isPasswordMatch);

            if (isPasswordMatch) {
                return Optional.of(findMember);
            } else {
                log.info("로그인 실패 - userId : {} 비밀번호 불일치", memberDto.getUserId());
                return Optional.empty();
            }
        } catch (IllegalArgumentException e) {
            log.info("로그인 실패 - userId : {} 존재하지 않음", memberDto.getUserId());
            return Optional.empty();
        }
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
            log.info("로그인 실패 {}", memberId);
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
                log.info("로그인 실패 , 비밀번호 불일치 {}", memberId);
                return Optional.empty();
            }
        } catch (IllegalArgumentException e) {
            loginAttempts.put(memberId, attempts + 1);
            log.info("로그인 실패, 존재하지 않음", memberId);
            return Optional.empty();
        }
    }

    public void logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
            log.info("로그아웃 성공");
        }
    }

    /*
     MemberDto를 데이터베이스에 저장하거나 비즈니스 로직에서 사용하기 위해 Member 엔티티로 변환할 때 사용
     엔티티는 일반적으로 데이터베이스와의 상호작용을 담당하며,
     비즈니스 로직에 따라 데이터베이스에 저장되거나 업데이트됨.
     */
    private Member convertToEntity(MemberDto memberDto) {
        return Member.builder()
                .id(memberDto.getId())
                .userId(memberDto.getUserId())
                .username(memberDto.getUsername())
                .age(memberDto.getAge())
                .address(memberDto.getAddress())
                .grade(memberDto.getGrade())
                .paymentMethod(memberDto.getPaymentMethod())
                .memberStatus(memberDto.getMemberStatus())
                .password(memberDto.getPassword()) // 이미 해시화된 비밀번호를 사용
                .build();
    }

    // Member -> MemberDto 객체로 변환
    // Member 객체를 클라이언트에게 전달하기 위해 DTO로 변환할 때 사용
    private MemberDto convertToDto(Member member) {
        return MemberDto.builder()
                .id(member.getId())
                .userId(member.getUserId())
                .username(member.getUsername())
                .age(member.getAge())
                .address(member.getAddress())
                .grade(member.getGrade())
                .paymentMethod(member.getPaymentMethod())
                .memberStatus(member.getMemberStatus())
                .password(member.getPassword())
                .build();
    }
}
