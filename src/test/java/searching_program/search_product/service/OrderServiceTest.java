package searching_program.search_product.service;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.weaver.ast.Or;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import searching_program.search_product.domain.*;
import searching_program.search_product.dto.*;
import searching_program.search_product.repository.CategoryRepository;
import searching_program.search_product.repository.ItemRepository;
import searching_program.search_product.repository.MemberRepository;
import searching_program.search_product.repository.OrderRepository;
import searching_program.search_product.type.OrderStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static searching_program.search_product.type.OrderStatus.*;

@Slf4j
@SpringBootTest
class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private DtoEntityConverter converter;

    private OrderDto orderDto;
    private MemberDto memberDto;
    private ItemDto itemDto;
    private OrderItemDto orderItemDto;
    private CategoryDto categoryDto;


    @BeforeEach
    void setUp() {
        // 회원 저장
        Member member = memberRepository.save(Member.builder()
                .userId("testUser")
                .username("터진입")
                .password(passwordEncoder.encode("test"))
                .age(30)
                .build());

        memberDto = converter.convertToMemberDto(member);

        // 카테고리 저장
        Category category = categoryRepository.save(Category.builder()
                .name("Test Category")
                .build());

        // 아이템 저장
        Item item = itemRepository.save(Item.builder()
                .itemName("Test Item")
                .itemPrice(100)
                .stock(1000)
                .category(category) // 저장된 카테고리 설정
                .build());

        itemDto = converter.convertToItemDto(item);

        // 주문 DTO 생성
        orderDto = OrderDto.builder()
                .userId(memberDto.getUserId())
                .orderDate(LocalDateTime.now())
                .orderItems(new ArrayList<>())
                .status(ORDERED)
                .orderStatusHistories(new ArrayList<>())
                .build();

        // 주문 항목 DTO 생성
        orderItemDto = OrderItemDto.builder()
                .itemId(itemDto.getId())
                .price(200)
                .quantity(200)
                .build();

        orderDto.getOrderItems().add(orderItemDto);
    }

    @Test
    @Transactional
    void createOrder() {
        OrderDto createOrder = orderService.createOrder(orderDto, memberDto);

        assertNotNull(createOrder.getId());
        assertEquals(ORDERED, createOrder.getStatus());
        assertEquals(1, createOrder.getOrderItems().size());
    }

    @Test
    @Transactional
    void findOrdersByMember() {
        Page<OrderDto> orderDtos = orderService.findOrdersByMember(memberDto.getUserId(), 0, 10);
        assertEquals(1, orderDtos.getTotalElements());
        assertEquals(memberDto.getUserId(), orderDtos.getContent().get(0).getUserId());
    }

    @Test
    @Transactional
    void cancel() {
        OrderDto createdOrder = orderService.createOrder(orderDto, memberDto);
        OrderDto cancelOrder = orderService.cancelOrder(createdOrder.getId());

        assertEquals(CANCELED, cancelOrder.getStatus());
    }

    @Test
    @Transactional
    void updateOrder() {
        OrderDto createdOrder = orderService.createOrder(orderDto, memberDto);
        OrderItemDto orderItemDtosUpdate = createdOrder.getOrderItems().get(0);

        orderService.updateOrder(orderItemDtosUpdate.getId(), 5, 500);

        OrderDto updatedOrder = orderService.findOrderById(createdOrder.getId());
        OrderItemDto updatedOrderItem = updatedOrder.getOrderItems().get(0);

        assertEquals(5, updatedOrderItem.getQuantity());
        assertEquals(500, updatedOrderItem.getPrice());

    }

    /**
     * 주문 상태를 저장시, 동일한 생태가 중복으로 저장
     * saveOrderStatusHistory 메소드에서 주문 상태를 기록을 추가할 때 중복 여부 확인X
     * saveOrderStatusHistory -> 해당 상태가 이미 기록 되었는지 확인하는 로직 추가.
     */
    @Test
    @Transactional
    void trackOrders() {
        OrderDto createdOrder = orderService.createOrder(orderDto, memberDto);
        List<OrderStatusHistoryDto> histories = orderService.trackOrder(createdOrder.getId());

        assertEquals(1, histories.size()); // 최초 상태 기록은 1개여야 합니다.
        assertEquals(ORDERED, histories.get(0).getStatus());

        // 상태 업데이트 후 상태 기록 확인
        Orders orders = orderRepository.findById(createdOrder.getId())
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

        orderService.saveOrderStatusHistory(orders, SHIPPED);
        histories = orderService.trackOrder(createdOrder.getId());

        assertEquals(2, histories.size()); // 상태 기록이 2개가 되었는지 확인
        assertEquals(SHIPPED, histories.get(1).getStatus());
    }
}