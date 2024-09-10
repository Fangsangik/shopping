package searching_program.search_product.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import searching_program.search_product.domain.Member;
import searching_program.search_product.dto.DtoEntityConverter;
import searching_program.search_product.dto.MemberDto;
import searching_program.search_product.error.CustomError;
import searching_program.search_product.repository.MemberRepository;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // 실제 데이터베이스 사용
class LoginServiceTest {

    @Autowired
    private MemberService memberService;

    @Autowired
    private LoginService loginService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private DtoEntityConverter converter;

    private MemberDto member;

    @BeforeEach
    void setUp() {
        // Given
        memberRepository.deleteAll();

        this.member = MemberDto.builder()
                .userId("userId")
                .password("testPassword")  // 실제 비밀번호로 설정
                .username("Test User")
                .build();

        Member entityMember = converter.convertToMemberEntity(member);
        entityMember.setPassword(passwordEncoder.encode(member.getPassword()));
        Member savedMember = memberRepository.save(entityMember);
        this.member = converter.convertToMemberDto(savedMember);
    }

    @Test
    @Transactional
    void registerMember() {
        // Given
        MemberDto memberDto = new MemberDto();
        memberDto.setUserId("newUser");
        memberDto.setPassword("testPassword");
        memberDto.setUsername("Test User");

        // When
        MemberDto createMember = memberService.createMember(memberDto);

        // Then
        assertThat(createMember).isNotNull();
        assertThat(createMember.getUserId()).isEqualTo("newUser");

        Member savedMember = memberRepository.findByUserId(memberDto.getUserId()).orElse(null);
        assertThat(savedMember).isNotNull();
        assertThat(passwordEncoder.matches("testPassword", savedMember.getPassword())).isTrue();
    }

    @Test
    @Transactional
    void registerMember_nullUserId() {
        member.setUserId(null);

        assertThrows(CustomError.class, () -> {
            memberService.createMember(member);
        });
    }

    @Test
    void loginSuccess() {

        // When
        Optional<MemberDto> rst = loginService.loginCheck(member.getUserId(), "testPassword"); // 원시 비밀번호 사용

        // Then
        assertThat(rst).isPresent();
        assertThat(rst.get().getUserId()).isEqualTo(member.getUserId());
    }

    @Test
    void wrongPassword() {
        // Given
        MemberDto memberDto = MemberDto.builder()
                .userId("userId")
                .password("wrongPassword") // 잘못된 비밀번호
                .build();

        // When
        Optional<MemberDto> rst = loginService.loginCheck(memberDto.getUserId(), memberDto.getPassword());

        // Then
        assertThat(rst).isNotPresent();
    }

    @Test
    void loginTryCountLimit() {
        // Given
        // 잘못된 비밀번호로 테스트할 DTO
        MemberDto wrongPasswordDto = MemberDto.builder()
                .userId("userId")
                .password("wrongPassword") // 잘못된 비밀번호 사용
                .build();

        // When - 비밀번호 4회 틀리게 입력
        for (int i = 0; i < 4; i++) {
            Optional<MemberDto> loginResult = loginService.loginCheck(wrongPasswordDto.getUserId(), wrongPasswordDto.getPassword());
            assertThat(loginResult).isEmpty(); // 로그인 실패해야 함
        }

        // Then - 5번째 시도 시 계정이 잠기는지 확인
        CustomError exception = assertThrows(CustomError.class, () -> {
            loginService.loginCheck(wrongPasswordDto.getUserId(), wrongPasswordDto.getPassword());
        });

        // 예외 메시지 확인
        assertThat(exception.getMessage()).isEqualTo("계정이 잠겼습니다");

        // 로그인 시도 횟수 확인
        Member savedMember = memberRepository.findByUserId("userId").orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
        int loginAttempts = loginService.getLoginAttempts(savedMember.getId());
        assertThat(loginAttempts).isEqualTo(5); // 5회 시도 후 계정 잠김 확인

    }


    @Test
    void logout() {
        // Given
        HttpServletRequest request = new MockHttpServletRequest();
        HttpSession session = request.getSession();
        session.setAttribute("member", member.getId());

        // When
        loginService.logout(request);

        // Then - 세션이 무효화되었는지 확인
        assertThrows(IllegalStateException.class, () -> {
            session.getAttribute("member");
        });
    }
}