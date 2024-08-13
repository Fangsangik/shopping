package searching_program.search_product.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searching_program.search_product.domain.Member;
import searching_program.search_product.dto.MemberDto;
import searching_program.search_product.repository.MemberRepository;

import java.util.List;
import java.util.Optional;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {

    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;

    // 비밀번호 해시화 메서드
    private String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    public MemberDto save(MemberDto memberDto) {
        // 비밀번호 해시화
        memberDto.setPassword(encodePassword(memberDto.getPassword()));

        // DTO를 엔티티로 변환
        Member member = convertToEntity(memberDto);

        // 엔티티 저장
        Member savedMember = memberRepository.save(member);

        // 저장된 엔티티를 DTO로 변환하여 반환
        return convertToDto(savedMember);
    }

    public MemberDto createMember(MemberDto memberDto) {
        log.info("회원 생성 요청: userId={}", memberDto.getUserId());

        // userId 중복 확인
        if (memberRepository.existsByUserId(memberDto.getUserId())) {
            log.error("회원 생성 실패: userId {}는 이미 존재합니다.", memberDto.getUserId());
            throw new IllegalArgumentException("아이디 값이 이미 존재합니다.");
        }

        validateMemberDto(memberDto);

        // 비밀번호 해시화
        String encodedPassword = passwordEncoder.encode(memberDto.getPassword());
        log.info("Encoded password: {}", encodedPassword);
        memberDto.setPassword(encodedPassword);

        Member newMember = convertToEntity(memberDto);
        Member savedMember = memberRepository.save(newMember);
        log.info("회원 생성 성공: ID={}", savedMember.getId());
        return convertToDto(savedMember);
    }


    public MemberDto updateMember(Long id, MemberDto memberDto) {
        log.info("회원 업데이트 요청: ID={}", id);

        if (!id.equals(memberDto.getId())) {
            log.error("회원 업데이트 실패: 요청된 ID와 DTO의 ID가 일치하지 않습니다.");
            throw new IllegalArgumentException("아이디가 일치하지 않습니다.");
        }

        Member existingMember = memberRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("회원 업데이트 실패: ID {}에 해당하는 회원이 존재하지 않습니다.", id);
                    return new IllegalArgumentException("회원이 존재하지 않습니다.");
                });

        existingMember.setUserId(memberDto.getUserId());
        existingMember.setUsername(memberDto.getUsername());
        existingMember.setAddress(memberDto.getAddress());
        existingMember.setAge(memberDto.getAge());
        existingMember.setGrade(memberDto.getGrade());
        existingMember.setPaymentMethod(memberDto.getPaymentMethod());

        // 비밀번호가 제공된 경우 해시화하여 저장
        if (memberDto.getPassword() != null && !memberDto.getPassword().isEmpty()) {
            existingMember.setPassword(encodePassword(memberDto.getPassword()));
        }

        Member updatedMember = memberRepository.save(existingMember);
        log.info("회원 업데이트 성공: ID={}", updatedMember.getId());
        return convertToDto(updatedMember);
    }

    public void deleteMember(Long id, String password) {
        log.info("회원 삭제 요청: ID={}", id);

        Member existingMember = memberRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("회원 삭제 실패: ID {}에 해당하는 회원이 존재하지 않습니다.", id);
                    return new IllegalArgumentException("회원이 존재하지 않습니다.");
                });

        if (!passwordEncoder.matches(password, existingMember.getPassword())) {
            log.error("회원 삭제 실패: 비밀번호가 일치하지 않습니다.");
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        memberRepository.deleteById(id);
        log.info("회원 삭제 성공: ID={}", id);
    }

    public List<MemberDto> findAll() {
        log.info("모든 회원 조회 요청");
        List<Member> members = memberRepository.findAll();
        return members.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    public Optional<MemberDto> findById(Long id) {
        log.info("회원 조회 요청: ID={}", id);
        return memberRepository.findById(id).map(this::convertToDto);
    }

    public Optional<MemberDto> findByUserId(String userId) {
        log.info("회원 조회 요청: userId={}", userId);
        return memberRepository.findByUserId(userId).map(this::convertToDto);
    }

    public List<MemberDto> findByName(String name) {
        log.info("이름으로 회원 조회 요청: 이름={}", name);
        List<Member> members = memberRepository.findByUsernameContaining(name);
        return members.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    public List<MemberDto> findByAgeGreaterThan(int age) {
        log.info("나이로 회원 조회 요청: 나이={}", age);
        List<Member> members = memberRepository.findByAgeGreaterThan(age);
        return members.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    public void deactivateMember(Long id) {
        log.info("회원 비활성화 요청: ID={}", id);
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("회원 비활성화 실패: ID {}에 해당하는 회원이 존재하지 않습니다.", id);
                    return new IllegalArgumentException("회원이 존재하지 않습니다.");
                });

        member.deactivate();
        memberRepository.save(member);
        log.info("회원 비활성화 성공: ID={}", id);
    }

    public void reactivateMember(Long id) {
        log.info("회원 활성화 요청: ID={}", id);
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("회원 활성화 실패: ID {}에 해당하는 회원이 존재하지 않습니다.", id);
                    return new IllegalArgumentException("회원이 존재하지 않습니다.");
                });

        member.reactivate();
        memberRepository.save(member);
        log.info("회원 활성화 성공: ID={}", id);
    }

    public void changePassword(Long id, String oldPassword, String newPassword) {
        log.info("비밀번호 변경 요청: ID={}", id);
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("비밀번호 변경 실패: ID {}에 해당하는 회원이 존재하지 않습니다.", id);
                    return new IllegalArgumentException("회원이 존재하지 않습니다.");
                });

        if (!passwordEncoder.matches(oldPassword, member.getPassword())) {
            log.error("비밀번호 변경 실패: 현재 비밀번호가 일치하지 않습니다.");
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        member.setPassword(encodePassword(newPassword));
        memberRepository.save(member);
        log.info("비밀번호 변경 성공: ID={}", id);
    }

    public void lockMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
        member.setLock(true);
        memberRepository.save(member);
    }

    private void validateMemberDto(MemberDto memberDto) {
        if (memberDto.getUsername() == null || memberDto.getUsername().isEmpty()) {
            log.error("유효성 검사 실패: 이름은 필수 입력 사항입니다.");
            throw new IllegalArgumentException("이름은 필수 입력 사항입니다.");
        }
        if (memberDto.getAge() < 0) {
            log.error("유효성 검사 실패: 나이는 음수가 될 수 없습니다.");
            throw new IllegalArgumentException("나이는 음수가 될 수 없습니다.");
        }
        if (memberDto.getUserId() == null || memberDto.getUserId().isEmpty()) {
            log.error("유효성 검사 실패: userId 값은 Null일 수 없습니다.");
            throw new IllegalArgumentException("userId 값은 Null일 수 없습니다.");
        }
    }

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
                .build();
    }

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
                .password(memberDto.getPassword())
                .build();
    }
}
