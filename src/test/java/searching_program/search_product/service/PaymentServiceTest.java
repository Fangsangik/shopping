package searching_program.search_product.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import searching_program.search_product.domain.Member;
import searching_program.search_product.domain.Orders;
import searching_program.search_product.dto.DtoEntityConverter;
import searching_program.search_product.dto.MemberDto;
import searching_program.search_product.dto.OrderDto;
import searching_program.search_product.dto.PaymentDto;
import searching_program.search_product.repository.MemberRepository;
import searching_program.search_product.repository.OrderRepository;
import searching_program.search_product.repository.PaymentRepository;
import searching_program.search_product.type.OrderStatus;
import searching_program.search_product.type.PaymentStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static searching_program.search_product.type.PaymentStatus.COMPLETED;

@SpringBootTest
public class PaymentServiceTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private DtoEntityConverter converter;

    private MemberDto memberDto;
    private OrderDto orderDto;

    @BeforeEach
    void setUp() {
        // Test용 회원 및 주문 데이터 생성
        Member member = memberRepository.save(Member.builder()
                .userId("testUser")
                .username("Test User")
                .payments(new ArrayList<>())
                .age(30)
                .address("Seoul, Korea")
                .build());

        memberDto = converter.convertToMemberDto(member);

        Orders orders = orderRepository.save(Orders.builder()
                .member(member)
                .orderItems(new ArrayList<>())
                .payments(new ArrayList<>())
                .statusHistory(new ArrayList<>())
                .orderDate(LocalDateTime.now())
                .status(OrderStatus.ORDERED)
                .build());

        orderDto = converter.convertToOrderDto(orders);
    }

    @Test
    @Transactional
    void testProcessPayment() {
        // Given
        double amount = 100.0;

        // 결제를 생성하고 성공 또는 실패 상태로 설정
        PaymentDto processPayment = paymentService.processPayment(memberDto.getId(), orderDto.getId(), amount);

        // 생성된 결제가 존재하고 상태가 FAILED 또는 COMPLETED인지 확인
        assertNotNull(processPayment, "결제 정보가 null이면 안됩니다.");
        assertTrue(processPayment.getStatus() == PaymentStatus.FAILED || processPayment.getStatus() == PaymentStatus.COMPLETED,
                "결제 상태가 FAILED 또는 COMPLETED이어야 합니다.");
    }
    /**
     * TODO : 실패 case 다시 검증 하기
     */
//    @Test
//    @Transactional
//    void testCancelPayment_Success() {
//        // Given
//        double amount = 100.0;
//
//        // 결제를 생성하고 `PENDING` 상태로 설정
//        PaymentDto processPayment = paymentService.processPayment(memberDto.getId(), orderDto.getId(), amount);
//
//        // 생성된 결제가 존재하고 `PENDING` 상태인지 확인
//        assertNotNull(processPayment, "결제 정보가 null이면 안됩니다.");
//        assertEquals(PaymentStatus.PENDING, processPayment.getStatus(), "결제 상태가 PENDING이어야 합니다.");
//
//        // When
//        PaymentDto cancelPayment = paymentService.cancelPayment(processPayment.getId());
//
//        // Then
//        assertNotNull(cancelPayment, "취소된 결제 정보가 null이면 안됩니다.");
//        assertEquals(PaymentStatus.CANCELED, cancelPayment.getStatus(), "결제 상태가 CANCELED이어야 합니다.");
//    }
}
