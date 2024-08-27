package searching_program.search_product.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import searching_program.search_product.dto.PaymentDto;
import searching_program.search_product.error.CustomError;
import searching_program.search_product.service.PaymentService;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/process")
    public ResponseEntity<PaymentDto> processPayment(
            @RequestParam Long memberId,
            @RequestParam Long orderId,
            @RequestParam Double amount) {
        try {
            PaymentDto paymentDto = paymentService.processPayment(memberId, orderId, amount);
            log.info("결제 처리 완료: 회원 ID = {}, 주문 ID = {}, 금액 = {}", memberId, orderId, amount);
            return ResponseEntity.ok(paymentDto);
        } catch (CustomError e) {
            log.error("결제 처리 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            log.error("예기치 않은 오류 발생: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/cancel")
    public ResponseEntity<PaymentDto> cancelPayment(
            @RequestParam Long memberId,
            @RequestParam Long paymentId) {
        try {
            PaymentDto paymentDto = paymentService.cancelPayment(memberId, paymentId);
            log.info("결제 취소 요청 성공: 회원 ID = {}, 결제 ID = {}", memberId, paymentId);
            return ResponseEntity.ok(paymentDto);
        } catch (CustomError e) {
            log.error("결제 취소 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            log.error("예기치 않은 오류 발생: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/refund")
    public ResponseEntity<PaymentDto> refundPayment(
            @RequestParam Long memberId,
            @RequestParam Long paymentId) {
        try {
            PaymentDto paymentDto = paymentService.refundPayment(memberId, paymentId);
            log.info("환불 요청 성공: 회원 ID = {}, 결제 ID = {}", memberId, paymentId);
            return ResponseEntity.ok(paymentDto);
        } catch (CustomError e) {
            log.error("환불 처리 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            log.error("예기치 않은 오류 발생: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/payment/{memberId}")
    public ResponseEntity<List<PaymentDto>> findPaymentByMember(@PathVariable Long memberId) {
        try {
            List<PaymentDto> paymentsByMember = paymentService.findPaymentsByMember(memberId);
            log.info("회원 아이디로 결제 내역 조회 성공: 회원 ID = {}", memberId);
            return ResponseEntity.ok(paymentsByMember);  // 조회된 결제 내역 반환
        } catch (CustomError e) {
            log.error("회원 결제 내역 조회 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            log.error("예기치 않은 오류 발생: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
