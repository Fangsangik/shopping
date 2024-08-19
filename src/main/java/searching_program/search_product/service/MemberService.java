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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;
    private final DtoEntityConverter converter;

    // 비밀번호 해시화 메서드
    private String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    @Transactional
    public MemberDto save(MemberDto memberDto) {
        // 비밀번호 해시화
        memberDto.setPassword(encodePassword(memberDto.getPassword()));

        // DTO를 엔티티로 변환
        Member member = converter.convertToMemberEntity(memberDto);

        // 엔티티 저장
        Member savedMember = memberRepository.save(member);

        // 저장된 엔티티를 DTO로 변환하여 반환
        return converter.convertToMemberDto(savedMember);
    }

    @Transactional
    public MemberDto createMember(MemberDto memberDto) {
        log.info("회원 생성 요청: userId={}", memberDto.getUserId());

        // userId 중복 확인
        if (memberRepository.existsByUserId(memberDto.getUserId())) {
            log.error("회원 생성 실패: userId {}는 이미 존재합니다.", memberDto.getUserId());
            throw new IllegalArgumentException("아이디 값이 이미 존재합니다.");
        }

        validateMemberDto(memberDto);

        if (memberDto.getPassword() == null) {
            throw new IllegalArgumentException("비밀번호가 null 일수 없습니다.");
        }

        // 비밀번호 해시화
        String encodedPassword = passwordEncoder.encode(memberDto.getPassword());
        log.info("Encoded password: {}", encodedPassword);
        memberDto.setPassword(encodedPassword);

        Member newMember = converter.convertToMemberEntity(memberDto);
        Member savedMember = memberRepository.save(newMember);
        log.info("회원 생성 성공: ID={}", savedMember.getId());
        return converter.convertToMemberDto(savedMember);

    }

    @Transactional
    public MemberDto updateMember(Long id, MemberDto memberDto) {
        log.info("회원 업데이트 요청: ID={}", id);

        Member existingMember = memberRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("회원 업데이트 실패: ID {}에 해당하는 회원이 존재하지 않습니다.", id);
                    return new IllegalArgumentException("회원이 존재하지 않습니다.");
                });

        log.debug("기존 회원 정보: {}", existingMember);

        if (!id.equals(memberDto.getId())) {
            log.error("회원 업데이트 실패: 요청된 ID와 DTO의 ID가 일치하지 않습니다.");
            throw new IllegalArgumentException("아이디가 일치하지 않습니다.");
        }

        // 비밀번호가 제공된 경우 해시화하여 저장
        if (memberDto.getPassword() != null && !memberDto.getPassword().isEmpty()) {
            String encodedPassword = encodePassword(memberDto.getPassword());
            existingMember.setPassword(encodedPassword);
            log.debug("비밀번호가 변경되었습니다: {}", encodedPassword);
        }

        if (memberDto.getUserId() == null) {
            throw new IllegalArgumentException("UserId가 설정되어 있지 않습니다.");
        }

        updateMemberFields(memberDto, existingMember);

        Member updatedMember = memberRepository.save(existingMember);
        log.info("회원 업데이트 성공: ID={}", updatedMember.getId());
        return converter.convertToMemberDto(updatedMember);
    }

    private static void updateMemberFields(MemberDto memberDto, Member existingMember) {
        //리펙토링 할때 Set값 변경하기.
        existingMember.setUserId(memberDto.getUserId());
        existingMember.setUsername(memberDto.getUsername());
        existingMember.setAddress(memberDto.getAddress());
        existingMember.setAge(memberDto.getAge());
        existingMember.setGrade(memberDto.getGrade());
        existingMember.setPaymentMethod(memberDto.getPaymentMethod());
    }

    @Transactional
    public void deleteMember(Long id, String password) {
        log.info("회원 삭제 요청 처리 시작: ID={}", id);

        Member existingMember = memberRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("회원 삭제 실패: ID {}에 해당하는 회원이 존재하지 않습니다.", id);
                    return new IllegalArgumentException("회원이 존재하지 않습니다.");
                });

        log.debug("입력된 비밀번호와 저장된 비밀번호 비교 중...");
        log.debug("저장된 비밀번호: {}", existingMember.getPassword());

        if (!passwordEncoder.matches(password, existingMember.getPassword())) {
            log.error("회원 삭제 실패: 비밀번호가 일치하지 않습니다.");
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        log.debug("비밀번호 일치 확인 완료, 회원 삭제 진행 중...");
        memberRepository.deleteById(id);
        log.info("회원 삭제 성공: ID={}", id);
    }

    @Transactional(readOnly = true)
    public List<MemberDto> findAll() {
        log.info("모든 회원 조회 요청");
        List<Member> members = memberRepository.findAll();
        return members.stream().map(converter::convertToMemberDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<MemberDto> findById(Long id) {
        log.info("회원 조회 요청: ID={}", id);
        return memberRepository.findById(id).map(converter::convertToMemberDto);
    }

    @Transactional(readOnly = true)
    public Optional<MemberDto> findByUserId(String userId) {
        log.info("회원 조회 요청: userId={}", userId);
        return memberRepository.findByUserId(userId).map(converter::convertToMemberDto);
    }

    @Transactional(readOnly = true)
    public List<MemberDto> findByName(String name) {
        log.info("이름으로 회원 조회 요청: 이름={}", name);
        List<Member> members = memberRepository.findByUsernameContaining(name);
        return members.stream().map(converter::convertToMemberDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MemberDto> findByAgeGreaterThan(int age) {
        log.info("나이로 회원 조회 요청: 나이={}", age);
        List<Member> members = memberRepository.findByAgeGreaterThan(age);
        return members.stream().map(converter::convertToMemberDto).collect(Collectors.toList());
    }

    @Transactional
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

    @Transactional
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

    @Transactional
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

    @Transactional
    public void lockMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
        member.setLock(true);
        memberRepository.save(member);
    }

    @Transactional(readOnly = true)
    public List<MemberDto> findAllMembers() {
       return memberRepository.findAll()
               .stream()
               .map(member -> new MemberDto(
                member.getId(),
                member.getUserId(),
                member.getUsername(),
                member.getAge(),
                member.getPassword(),
                member.isLock(),
                member.getAddress(),
                member.getBirth(),
                member.getCreatedAt(),
                member.getDeletedAt(),
                member.getPaymentMethod(),
                member.getMemberStatus(),
                member.getGrade()
        ))
               .collect(Collectors.toList());
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
}
