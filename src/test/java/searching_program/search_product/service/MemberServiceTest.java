package searching_program.search_product.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import searching_program.search_product.domain.Member;
import searching_program.search_product.dto.MemberDto;
import searching_program.search_product.repository.MemberRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static searching_program.search_product.type.Grade.*;
import static searching_program.search_product.type.MemberStatus.*;

@Transactional
@SpringBootTest
@RunWith(SpringRunner.class)
class MemberServiceTest {

    @Autowired
    private MemberService memberService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MemberRepository memberRepository;

    private Member member;

    @BeforeEach
    void setUp() {
        memberRepository.deleteAll();  // 모든 데이터를 삭제하여 중복 삽입 방지
        member = Member.builder()
                .age(30)
                .userId("uniqueUserId")
                .password(passwordEncoder.encode("123"))
                .memberStatus(ACTIVE)
                .grade(VIP)
                .address("123")
                .build();


        this.member = memberRepository.save(member);
    }

    @Test
    @Transactional
    void save() {
        assertThat(member.getUserId()).isEqualTo("uniqueUserId");
    }

    @Test
    void findById() {
        Optional<MemberDto> findMemberById = memberService.findById(member.getId());

        if (!findMemberById.isPresent()) {
            throw new IllegalArgumentException("회원 정보를 찾을 수 없습니다.");
        }

        assertThat(findMemberById.get().getId()).isEqualTo(5L);
    }


    @Test
    @Transactional
    void createMember() {
        MemberDto createMember = MemberDto.builder()
                .userId("newUserId")
                .username("터진입")
                .password("password123")
                .age(30)
                .address("Seoul, Korea")
                .build();

        MemberDto savedMember = memberService.createMember(createMember);
        assertThat(savedMember.getUserId()).isEqualTo(createMember.getUserId());
    }

    @Test
    @Transactional
    void updateMember() {
        Long memberId = member.getId();

        MemberDto updateMember = MemberDto.builder()
                .id(memberId)
                .userId("newUserId")
                .username("터진입")
                .password("password123")
                .age(30)
                .address("Seoul, Korea")
                .build();

        MemberDto updated = memberService.updateMember(updateMember.getId(), updateMember);
        assertThat(updated.getUserId()).isEqualTo(updateMember.getUserId());
    }

    @Test
    @Transactional
    void deleteMember() {
        Long memberId = member.getId();

        memberService.deleteMember(memberId, "123");
        Optional<Member> deletedMember = memberRepository.findById(memberId);
        assertThat(deletedMember).isEmpty();
    }

    @Test
    @Transactional
    void findAll() {
        Long memberId = member.getId();
        List<MemberDto> findAll = memberService.findAll();
        assertThat(findAll)
                .hasSize(1)
                .extracting(MemberDto::getId)
                .contains(memberId);
    }

    @Test
    @Transactional
    void findByAgeGreaterThan() {
        // Given: 기본 설정에서 저장된 회원의 나이와 다른 나이의 회원을 추가
        memberRepository.save(Member.builder()
                .userId("anotherUser")
                .password("password")
                .age(member.getAge() + 1)  // 나이가 더 큰 회원을 저장
                .address("Another address")
                .build());

        Integer memberAge = member.getAge();
        List<MemberDto> byAgeGreaterThan = memberService.findByAgeGreaterThan(memberAge - 1);
        assertThat(byAgeGreaterThan).isNotEmpty();
        assertThat(byAgeGreaterThan).allMatch(memberDto -> memberDto.getAge() > memberAge - 1);
        assertThat(byAgeGreaterThan).extracting(MemberDto::getId).contains(member.getId());
    }

    @Test
    @Transactional
    void deactivateMember() {
        Long memberId = member.getId();

        memberService.deactivateMember(memberId);
        assertThat(member.getMemberStatus()).isEqualTo(UN_ACTIVE);
    }

    @Test
    @Transactional
    void reactivateMember() {
        Long memberId = member.getId();

        memberService.reactivateMember(memberId);
        assertThat(member.getMemberStatus()).isEqualTo(ACTIVE);
    }

    @Test
    @Transactional
    void changePassword() {
        Long memberId = member.getId();
        String oldPassword = "123";
        String newPassword = "new 123";
        memberService.changePassword(memberId, oldPassword, newPassword);

        Member updatePasswordMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원 존재하지 않습니다."));

        assertThat(passwordEncoder.matches(newPassword, updatePasswordMember.getPassword())).isTrue();

    }

    @Test
    @Transactional
    void lockMember() {

        Long userId = member.getId();
        memberService.lockMember(userId);

        Member lockMember = memberRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        assertThat(lockMember.isAccountLock()).isTrue();
    }
}