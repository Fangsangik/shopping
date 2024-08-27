package searching_program.search_product.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searching_program.search_product.domain.Member;
import searching_program.search_product.domain.Orders;
import searching_program.search_product.domain.Payment;
import searching_program.search_product.dto.DtoEntityConverter;
import searching_program.search_product.dto.PaymentDto;
import searching_program.search_product.error.CustomError;
import searching_program.search_product.repository.MemberRepository;
import searching_program.search_product.repository.OrderRepository;
import searching_program.search_product.repository.PaymentRepository;
import searching_program.search_product.type.ErrorCode;
import searching_program.search_product.type.PaymentStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static searching_program.search_product.type.ErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final DtoEntityConverter converter;
    private final MemberRepository memberRepository;

    /**
     * 결제 처리 메서드
     */
    @Transactional
    public PaymentDto processPayment(Long memberId, Long orderId, Double amount) {
        validateAmount(amount); // 1. 결제 금액 검증

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomError(USER_NOT_FOUND));

        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomError(ORDER_NOT_FOUND));

        Payment payment = Payment.builder()
                .member(member)
                .order(order)
                .amount(amount)
                .paymentStatus(PaymentStatus.PENDING)
                .paymentDate(LocalDateTime.now())
                .build();

        if (!authorizePayment(payment)) {
            payment.setPaymentStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            return converter.convertToPaymentDto(payment);
        }

        try {
            simulatePaymentGatewayProcessing(payment);
            payment.setPaymentStatus(PaymentStatus.COMPLETED);
        } catch (Exception e) {
            payment.setPaymentStatus(PaymentStatus.FAILED);
            log.error("결제 처리 중 오류 발생: {}", e.getMessage());
        }

        Payment savedPayment = paymentRepository.save(payment);
        return converter.convertToPaymentDto(savedPayment);
    }

    /**
     * 결제 취소 메서드
     */
    @Transactional
    public PaymentDto cancelPayment(Long memberId, Long paymentId) {
        // 결제 ID로 결제를 조회
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new CustomError(PAYMENT_NOT_FOUND));

        // 결제가 해당 회원의 결제인지 확인
        if (!payment.getMember().getId().equals(memberId)) {
            log.error("결제 취소 실패: 회원 ID {}는 결제 ID {}의 소유자가 아닙니다.", memberId, paymentId);
            throw new CustomError(USER_NOT_FOUND);
        }

        // 결제 상태가 완료된 경우 결제 취소 불가
        if (payment.getPaymentStatus() == PaymentStatus.COMPLETED) {
            log.error("결제 취소 실패: 이미 완료된 결제 ID {}", paymentId);
            throw new CustomError(PAYMENT_CANNOT_BE_CANCELED);
        }

        // 결제 상태가 PENDING 또는 AUTHORIZED가 아닌 경우 결제 취소 불가
        if (payment.getPaymentStatus() != PaymentStatus.PENDING && payment.getPaymentStatus() != PaymentStatus.AUTHORIZED) {
            throw new CustomError(PAYMENT_CANCELED);
            log.error("결제 취소 실패: 취소할 수 없는 상태인 결제 ID {}", paymentId);
            throw new CustomError(PAYMENT_CANNOT_BE_CANCELED);
        }

        // 결제 상태를 CANCELED로 설정
        payment.setPaymentStatus(PaymentStatus.CANCELED);
        paymentRepository.save(payment);

        log.info("결제 취소 완료: Payment ID: {}", payment.getId());
        return converter.convertToPaymentDto(payment);
    }

    /**
     * 결제 환불 메서드
     */
    @Transactional
    public PaymentDto refundPayment(Long memberId, Long paymentId) {
        // 결제 ID로 결제를 조회
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new CustomError(PAYMENT_NOT_FOUND));

        if (payment.getPaymentStatus() != PaymentStatus.COMPLETED) {
            throw new CustomError(PAYMENT_REFUND);
        }

        // 실제 환불 로직 구현
        payment.setPaymentStatus(PaymentStatus.REFUNDED);
        log.info("결제가 성공적으로 환불되었습니다: Payment ID: {}", payment.getId());

        paymentRepository.save(payment);
        return converter.convertToPaymentDto(payment);
    }

    /**
     * 회원별 결제 내역 조회 메서드
     */
    @Transactional(readOnly = true)
    public List<PaymentDto> findPaymentsByMember(Long memberId) {
        List<Payment> payments = paymentRepository.findByMember_Id(memberId);
        return payments.stream()
                .map(converter::convertToPaymentDto)
                .collect(Collectors.toList());
    }

    // 민감한 데이터가 포함되지 않도록 로그를 개선합니다
    private void processRefundTransaction(Payment payment) {
        if (Math.random() > 0.1) {
            log.info("모의 결제 게이트웨이: 환불 성공 (Payment ID: {})", payment.getId());
        } else {
            throw new CustomError(PAYMENT_FAILED);
        }
    }

    private boolean authorizePayment(Payment payment) {
        // 실제로는 결제 게이트웨이 API를 호출해야 합니다.
        return Math.random() > 0.5;
    }

    private void simulatePaymentGatewayProcessing(Payment payment) {
        if (Math.random() > 0.5) {
            log.info("모의 결제: 성공 (Payment ID: {})", payment.getId());
        } else {
            throw new CustomError(PAYMENT_FAILED);
        }
    }

    /**
     * 결제 금액 유효성 검사
     */
    private void validateAmount(Double amount) {
        if (amount == null || amount <= 0) {
            throw new CustomError(PAYMENT_NOT_AVAILABLE);
        }
    }
}