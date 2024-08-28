package searching_program.search_product.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import searching_program.search_product.dto.MemberDto;
import searching_program.search_product.repository.MemberRepository;
import searching_program.search_product.service.CustomUserDetailsService;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Autowired
    public SecurityConfig(UserDetailsService userDetailsService, @Lazy PasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable() // 개발 환경에서만 비활성화
                .authorizeRequests()
                // 로그인과 회원가입, 로그아웃은 누구나 접근 가능
                .antMatchers("/api/auth/login", "/api/register", "/api/auth/logout").permitAll()
                // 로그인한 사용자라면 접근 가능 (ROLE_USER 또는 ROLE_ADMIN) -> 개발 모드에서는 permit으로 임시 설정
                .antMatchers("/members/**", "/bucket/**", "/category/**", "/favorite/**",
                        "/items/**", "/notification/**", "/orders/**", "/payment/**",
                        "/promotion/**", "/review/**").permitAll()
                // 특정 페이지는 로그인한 사용자 모두 접근 가능
                .antMatchers("/login", "/login/register").permitAll()
                // 관리자만 접근 가능
                .antMatchers("/delete/**").hasRole("ADMIN")
                // 그 외 모든 요청은 인증된 사용자만 접근 가능
                .anyRequest().authenticated()
                .and()
                .formLogin()
                .loginPage("/login")  // 커스텀 로그인 페이지 경로
                .usernameParameter("userId") // 폼에서 사용자 아이디 필드 이름
                .passwordParameter("password") // 폼에서 비밀번호 필드 이름
                .defaultSuccessUrl("/members", true) // 로그인 성공 후 이동할 기본 URL
                .failureUrl("/login?error") // 로그인 실패 시 이동할 URL
                .permitAll() // 로그인 폼은 모두 접근 가능
                .and()
                .logout()
                .logoutUrl("/api/logout") // 로그아웃 URL
                .permitAll(); // 로그아웃도 모두 접근 가능
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder);
    }
}