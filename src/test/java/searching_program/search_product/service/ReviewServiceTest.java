package searching_program.search_product.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import searching_program.search_product.domain.*;
import searching_program.search_product.dto.*;
import searching_program.search_product.repository.*;
import searching_program.search_product.type.OrderStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ReviewServiceTest {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private DtoEntityConverter converter;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private MemberDto memberDto;
    private ItemDto itemDto;
    private OrderDto orderDto;
    private CategoryDto categoryDto;


    @BeforeEach
    void setUp() {
        // 카테고리 생성 및 저장
        Category category = categoryRepository.save(Category.builder()
                .name("Electronics")
                .build());

        // 멤버 생성 및 저장
        Member member = memberRepository.save(Member.builder()
                .userId("testUser")
                .username("Test User")
                .password("password")
                .address("123 Test St")
                .build());

        // 아이템 생성 및 저장
        Item item = itemRepository.save(Item.builder()
                .itemName("Test Item")
                .itemPrice(1000)
                .category(category)
                .build());

        // 주문 생성 및 저장
        Orders order = orderRepository.save(Orders.builder()
                .member(member)
                .orderItems(new ArrayList<>())
                .statusHistory(new ArrayList<>())
                .orderDate(LocalDateTime.now())
                .status(OrderStatus.ORDERED)
                .build());

        // 주문 항목 생성 및 저장
        OrderItem orderItem = orderItemRepository.save(OrderItem.builder()
                .order(order)
                .item(item)
                .item(item)
                .quantity(1)
                .price(1000)
                .build());

        // DTO 변환 및 저장
        memberDto = converter.convertToMemberDto(member);
        itemDto = converter.convertToItemDto(item);
        orderDto = converter.convertToOrderDto(order);
    }

    @Test
    void addReview() {
        reviewService.addReview(orderDto.getId(), memberDto.getId(), itemDto.getId(), "GOOD", 10);

        Optional<Review> savedReview = reviewRepository.findByMemberIdAndItemId(memberDto.getId(), memberDto.getId());
        assertTrue(savedReview.isPresent(), "리뷰가 저장되었습니다.");

        Review review = savedReview.get();
        assertEquals("GOOD", review.getReviewText());
        assertEquals(10, review.getRate());
        assertEquals(memberDto.getId(), review.getMember().getId());
        assertEquals(itemDto.getId(), review.getItem().getId());
    }

    @Test
    void removeReview() {
        reviewService.addReview(orderDto.getId(), memberDto.getId(), itemDto.getId(), "GOOD", 10);

        reviewService.removeReview(memberDto.getId(), itemDto.getId());

        // 리뷰가 제거되었는지 확인
        Optional<Review> removeReview = reviewRepository.findByMemberIdAndItemId(memberDto.getId(), itemDto.getId());

        // 리뷰가 더 이상 존재하지 않아야 합니다.
        assertTrue(removeReview.isEmpty(), "리뷰가 성공적으로 삭제되지 않았습니다.");
    }

    @Test
    void editReview() {
        reviewService.addReview(orderDto.getId(), memberDto.getId(), itemDto.getId(), "GOOD", 10);

        reviewService.editReview(memberDto.getId(), itemDto.getId(), "나빠요", 1);
        Optional<Review> findReview = reviewRepository.findByMemberIdAndItemId(memberDto.getId(), itemDto.getId());
        assertTrue(findReview.isPresent());

        Review review = findReview.get();
        assertEquals("나빠요", findReview.get().getReviewText());
        assertEquals(1, findReview.get().getRate());
        assertEquals(memberDto.getId(), review.getMember().getId());
        assertEquals(itemDto.getId(), review.getItem().getId());

    }

    @Test
    void getReviewByItem() {
        reviewService.addReview(orderDto.getId(), memberDto.getId(), itemDto.getId(), "GOOD", 10);
        reviewService.addReview(orderDto.getId(), memberDto.getId(), itemDto.getId(), "not Bad", 5);
        List<Review> byItemId = reviewRepository.findByItemId(itemDto.getId());

        assertEquals(2, byItemId.size());
        assertTrue(byItemId.stream().anyMatch(review -> review.getReviewText().equals("GOOD")
                ), "첫 리뷰가 예상과 일치하지 않습니다.");

        assertTrue(byItemId.stream().anyMatch(review -> review.getReviewText().equals("not Bad")
        ), "두번째 리뷰가 예상과 일치하지 않습니다.");
    }
}