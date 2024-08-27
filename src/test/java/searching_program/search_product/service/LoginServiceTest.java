package searching_program.search_product.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import searching_program.search_product.domain.Member;
import searching_program.search_product.dto.DtoEntityConverter;
import searching_program.search_product.dto.MemberDto;
import searching_program.search_product.repository.MemberRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // 임베디드 데이터베이스로 교체하지 않도록 설정
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

    LoginServiceTest() {
    }

    @BeforeEach
    void setUp() {

        memberRepository.deleteAll();

        this.member = MemberDto.builder()
                .userId("userId")
                .password(passwordEncoder.encode("testPassword"))
                .build();

        Member entityMember = converter.convertToMemberEntity(member);
//        System.out.println("Before saving to DB, member password: " + entityMember.getPassword()); //convertToMemberDto 부분에 password 값 빠져있었음

        Member savedMember = memberRepository.save(entityMember);
        MemberDto dtoMember = converter.convertToMemberDto(savedMember);

//        System.out.println("After saving to DB, dtoMember password: " + dtoMember.getPassword());

//        System.out.println("savedMember = " + savedMember);
//        System.out.println("dtoMember = " + dtoMember);

        this.member = dtoMember;

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
        boolean isRegistered = loginService.registerMember(memberDto);

        // Then
        assertThat(isRegistered).isTrue();

        Member savedMember = memberRepository.findByUserId(memberDto.getUserId()).orElse(null);
        assertThat(savedMember).isNotNull();
        assertThat(passwordEncoder.matches("testPassword", savedMember.getPassword())).isTrue();
    }

    @Test
    @Transactional
    void registerMember_nullUserId() {
        member.setUserId(null);

        assertThrows(IllegalArgumentException.class, () -> {
            loginService.registerMember(member);
        });
    }

    @Test
    void loginSuccess() {
        MemberDto loginAttempt = MemberDto.builder()
                .userId("userId")
                .password("testPassword")
                .build();

        Optional<MemberDto> rst = loginService.loginCheck(loginAttempt.getUserId(), loginAttempt.getPassword());

        System.out.println("rst = " + rst);

        assertThat(rst).isPresent();
        assertThat(rst.get().getUserId()).isEqualTo(loginAttempt.getUserId());
    }

    @Test
    void wrong_password() {
        MemberDto memberDto = MemberDto.builder()
                .userId("userId")
                .password("123")
                .build();

        Optional<MemberDto> rst = loginService.loginCheck(memberDto.getUserId(), memberDto.getPassword());
        assertThat(rst).isNotPresent();
    }

    @Test
    void loginTryCountLimit() {
        MemberDto memberDto = MemberDto.builder()
                .id(member.getId())
                .password("wrongPassword")
                .build();

        for (int i = 0; i < 4; i++) {
            loginService.loginTryCountLimit(memberDto);
        }

        assertThat(loginService.loginTryCountLimit(memberDto)).isEmpty();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            loginService.loginTryCountLimit(memberDto);
        });
        assertThat(exception.getMessage()).isEqualTo("계정이 잠겼습니다.");

    }

    @Test
    void logout() {
        //실제 HttpServletRequest와 HttpSesion 객체를 사용하는 코드
        HttpServletRequest request = new MockHttpServletRequest();
        HttpSession session = request.getSession();
        session.setAttribute("member", 1L);

        loginService.logout(request);

        // 세션이 무효화되었는지 확인
        assertThrows(IllegalStateException.class, () -> {
            session.getAttribute("member");
        });
    }
}