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


@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {

    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;

    public MemberDto save(MemberDto memberDto) {
        // DTO를 엔티티로 변환
        Member member = Member.builder()
                .username(memberDto.getUsername())
                .age(memberDto.getAge())
                .address(memberDto.getAddress())
                .password(passwordEncoder.encode(memberDto.getPassword()))
                .birth(memberDto.getBirth())
                .createdAt(memberDto.getCreatedAt())
                .deletedAt(memberDto.getDeletedAt())
                .paymentMethod(memberDto.getPaymentMethod())
                .grade(memberDto.getGrade())
                .memberStatus(memberDto.getMemberStatus())
                .build();

        // 엔티티 저장
        Member savedMember = memberRepository.save(member);

        // 저장된 엔티티를 DTO로 변환하여 반환
        return convertToDto(savedMember);
    }

    public MemberDto createMember(MemberDto memberDto) {
        log.info("회원 생성 요청: ID={}", memberDto.getId());

        if (memberRepository.existsById(memberDto.getId())) {
            log.error("회원 생성 실패: ID {}는 이미 존재합니다.", memberDto.getId());
            throw new IllegalArgumentException("아이디 값이 이미 존재합니다.");
        }

        validateMemberDto(memberDto);

        String encodedPassword = passwordEncoder.encode(memberDto.getPassword());
        Member newMember = Member.builder()
                .username(memberDto.getUsername())
                .age(memberDto.getAge())
                .address(memberDto.getAddress())
                .password(encodedPassword)
                .grade(memberDto.getGrade())
                .paymentMethod(memberDto.getPaymentMethod())
                .memberStatus(memberDto.getMemberStatus())
                .build();

        Member savedMember = memberRepository.save(newMember);
        log.info("회원 생성 성공: ID={}", savedMember.getId());
        return convertToDto(savedMember);
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
        if (memberDto.getId() == null) {
            log.error("유효성 검사 실패: ID 값은 Null일 수 없습니다.");
            throw new IllegalArgumentException("ID 값은 Null일 수 없습니다.");
        }
    }

    public List<MemberDto> findAll() {
        log.info("모든 회원 조회 요청");
        List<Member> members = memberRepository.findAll();
        return members.stream().map(this::convertToDto).toList();
    }

    public Optional<MemberDto> findById(Long id) {
        log.info("회원 조회 요청: ID={}", id);
        return memberRepository.findById(id).map(this::convertToDto);
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

        existingMember.setUsername(memberDto.getUsername());
        existingMember.setAddress(memberDto.getAddress());
        existingMember.setAge(memberDto.getAge());
        existingMember.setGrade(memberDto.getGrade());
        existingMember.setPaymentMethod(memberDto.getPaymentMethod());

        if (memberDto.getPassword() != null && !memberDto.getPassword().isEmpty()) {
            existingMember.setPassword(passwordEncoder.encode(memberDto.getPassword()));
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

    public List<MemberDto> findByName(String name) {
        log.info("이름으로 회원 조회 요청: 이름={}", name);
        List<Member> members = memberRepository.findByUsernameContaining(name);
        return members.stream().map(this::convertToDto).toList();
    }

    public List<MemberDto> findByAgeGreaterThan(int age) {
        log.info("나이로 회원 조회 요청: 나이={}", age);
        List<Member> members = memberRepository.findByAgeGreaterThan(age);
        return members.stream().map(this::convertToDto).toList();
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

        member.setPassword(passwordEncoder.encode(newPassword));
        memberRepository.save(member);
        log.info("비밀번호 변경 성공: ID={}", id);
    }

    private MemberDto convertToDto(Member member) {
        return MemberDto.builder()
                .id(member.getId())
                .username(member.getUsername())
                .age(member.getAge())
                .address(member.getAddress())
                .grade(member.getGrade())
                .paymentMethod(member.getPaymentMethod())
                .memberStatus(member.getMemberStatus())
                .build();
    }
    // 이메일 인증 서비스 구현
}
