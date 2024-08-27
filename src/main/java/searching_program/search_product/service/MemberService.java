package searching_program.search_product.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searching_program.search_product.domain.Member;
import searching_program.search_product.dto.DtoEntityConverter;
import searching_program.search_product.dto.MemberDto;
import searching_program.search_product.error.CustomError;
import searching_program.search_product.repository.MemberRepository;
import searching_program.search_product.type.ErrorCode;

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

        Member savedMember = memberRepository.save(converter.convertToMemberEntity(memberDto));
        return converter.convertToMemberDto(savedMember);
    }

    @Transactional
    public MemberDto createMember(MemberDto memberDto) {
        log.info("회원 생성 요청: userId={}", memberDto.getUserId());

        validateMemberDto(memberDto);

        // userId 중복 확인
        if (memberRepository.existsByUserId(memberDto.getUserId())) {
            log.error("회원 생성 실패: userId {}는 이미 존재합니다.", memberDto.getUserId());
            throw new CustomError(ErrorCode.USER_DUPLICATE);
        }


        memberDto.setPassword(encodePassword(memberDto.getPassword()));
        Member savedMember = memberRepository.save(converter.convertToMemberEntity(memberDto));
        log.info("회원 생성 성공: ID={}", savedMember.getId());
        return converter.convertToMemberDto(savedMember);

    }

    @Transactional
    public MemberDto updateMember(Long id, MemberDto memberDto) {
        log.info("회원 업데이트 요청: ID={}", id);

        Member existingMember = findMemberById(id);

        log.debug("기존 회원 정보: {}", existingMember);

        if (!id.equals(memberDto.getId())) {
            log.error("회원 업데이트 실패: 요청된 ID와 DTO의 ID가 일치하지 않습니다.");
            throw new CustomError(ErrorCode.INVALID_INPUT_VALUE);
        }

        if (isPasswordProvided(memberDto)) {
            existingMember.setPassword(encodePassword(memberDto.getPassword()));
        }

        validateUserId(memberDto);
        updateMemberFields(memberDto, existingMember);

        // 기존 Member 객체를 업데이트된 DTO 객체로 변환
        log.info("회원 업데이트 성공: ID={}", existingMember.getId());
        return converter.convertToMemberDto(memberRepository.save(existingMember));
    }


    @Transactional
    public void deleteMember(Long id, String password) {
        log.info("회원 삭제 요청 처리 시작: ID={}", id);

        Member existingMember = findMemberById(id);

        log.debug("입력된 비밀번호와 저장된 비밀번호 비교 중...");
        log.debug("저장된 비밀번호: {}", existingMember.getPassword());

        if (!passwordEncoder.matches(password, existingMember.getPassword())) {
            log.error("회원 삭제 실패: 비밀번호가 일치하지 않습니다.");
            throw new CustomError(ErrorCode.PASSWORD_NOT_MATCH);
        }

        log.debug("비밀번호 일치 확인 완료, 회원 삭제 진행 중...");
        memberRepository.deleteById(id);
        log.info("회원 삭제 성공: ID={}", id);
    }

    @Transactional(readOnly = true)
    public List<MemberDto> findAll() {
        log.info("모든 회원 조회 요청");
        return memberRepository.findAll()
                .stream().map(converter::convertToMemberDto)
                .collect(Collectors.toList());
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
        return memberRepository.findByUsernameContaining(name)
                .stream().map(converter::convertToMemberDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MemberDto> findByAgeGreaterThan(int age) {
        log.info("나이로 회원 조회 요청: 나이={}", age);
        return memberRepository.findByAgeGreaterThan(age)
                .stream()
                .map(converter::convertToMemberDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deactivateMember(Long id) {
        log.info("회원 비활성화 요청: ID={}", id);
        Member member = findMemberById(id);
        member.deactivate();
        memberRepository.save(member);
        log.info("회원 비활성화 성공: ID={}", id);
    }

    @Transactional
    public void reactivateMember(Long id) {
        log.info("회원 활성화 요청: ID={}", id);
        Member member = findMemberById(id);
        member.reactivate();
        memberRepository.save(member);
        log.info("회원 활성화 성공: ID={}", id);
    }

    @Transactional
    public void changePassword(Long id, String oldPassword, String newPassword) {
        log.info("비밀번호 변경 요청: ID={}", id);
        Member member =findMemberById(id);

        if (!passwordEncoder.matches(oldPassword, member.getPassword())) {
            log.error("비밀번호 변경 실패: 현재 비밀번호가 일치하지 않습니다.");
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        member.setPassword(encodePassword(newPassword));
        memberRepository.save(member);
        log.info("비밀번호 변경 성공: ID={}", id);
    }

    @Transactional
    public void lockMember(Long id) {
        Member member = findMemberById(id);
        member.setAccountLock(true);
        memberRepository.save(member);
    }

    @Transactional(readOnly = true)
    public List<MemberDto> findAllMembers() {
        return memberRepository.findAll()
                .stream()
                .map(converter::convertToMemberDto)
                .collect(Collectors.toList());
    }

    private Member findMemberById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new CustomError(ErrorCode.USER_NOT_FOUND));
    }

    private void validateMemberDto(MemberDto memberDto) {
        if (memberDto.getUsername() == null || memberDto.getUsername().isEmpty()) {
            log.error("유효성 검사 실패: 이름은 필수 입력 사항입니다.");
            throw new CustomError(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (memberDto.getAge() < 0) {
            log.error("유효성 검사 실패: 나이는 음수가 될 수 없습니다.");
            throw new CustomError(ErrorCode.AGE_MUST_GREATER_THAN_ZERO);
        }
        if (memberDto.getUserId() == null || memberDto.getUserId().isEmpty()) {
            log.error("유효성 검사 실패: userId 값은 Null일 수 없습니다.");
            throw new CustomError(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private static void validateUserId(MemberDto memberDto) {
        if (memberDto.getUserId() == null) {
            throw new CustomError(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private boolean isPasswordProvided(MemberDto memberDto) {
        return memberDto.getPassword() != null && !memberDto.getPassword().isEmpty();
    }

    /**
     * TODO -> set값 사용 안하기
     */
    private static void updateMemberFields(MemberDto memberDto, Member existingMember) {
        existingMember.setUserId(memberDto.getUserId());
        existingMember.setUsername(memberDto.getUsername());
        existingMember.setAddress(memberDto.getAddress());
        existingMember.setAge(memberDto.getAge());
        existingMember.setGrade(memberDto.getGrade());
        existingMember.setPaymentMethod(memberDto.getPaymentMethod());
    }
}
