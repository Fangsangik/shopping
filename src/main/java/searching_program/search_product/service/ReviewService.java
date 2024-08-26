package searching_program.search_product.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searching_program.search_product.domain.Item;
import searching_program.search_product.domain.Member;
import searching_program.search_product.domain.Orders;
import searching_program.search_product.domain.Review;
import searching_program.search_product.dto.DtoEntityConverter;
import searching_program.search_product.dto.ReviewDto;
import searching_program.search_product.error.CustomError;
import searching_program.search_product.repository.ItemRepository;
import searching_program.search_product.repository.MemberRepository;
import searching_program.search_product.repository.OrderRepository;
import searching_program.search_product.repository.ReviewRepository;
import searching_program.search_product.type.ErrorCode;

import java.util.List;
import java.util.stream.Collectors;

import static searching_program.search_product.type.ErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;
    private final DtoEntityConverter converter;
    private final OrderRepository orderRepository;

    @Transactional
    public void addReview (Long orderId, Long memberId, Long itemId, String reviewText, int rate) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomError(USER_NOT_FOUND));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new CustomError(ITEM_NOT_FOUND));

        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomError(ORDER_NOT_FOUND));

        // 주문에 해당 아이탬이 있는지 확인하는 로직
        boolean itemInOrder = order.getOrderItems().stream()
                .anyMatch(orderItem -> orderItem.getItem().getId().equals(itemId));

        if (!itemInOrder) {
            throw new CustomError(ORDER_LIST_NOT_FOUND);
        }

        Review review = Review.builder()
                .member(member)
                .item(item)
                .reviewText(reviewText)
                .rate(rate)
                .build();
        reviewRepository.save(review);
    }

    @Transactional
    public void removeReview(Long memberId, Long itemId) {
        Review review = reviewRepository.findByMemberIdAndItemId(memberId, itemId)
                .orElseThrow(() -> new CustomError(REVIEW_NOT_FOUND));

        reviewRepository.delete(review);
    }

    @Transactional
    public void editReview(Long memberId, Long itemId, String newReviewText, int newRate){
        Review review = reviewRepository.findByMemberIdAndItemId(memberId, itemId)
                .orElseThrow(() -> new CustomError(REVIEW_NOT_FOUND));


        review.setReviewText(newReviewText);
        review.setRate(newRate);

        reviewRepository.save(review);
    }

    @Transactional(readOnly = true)
    public List<ReviewDto> getReviewsByItemId(Long itemId) {
        List<Review> reviews = reviewRepository.findByItemId(itemId);
        return reviews.stream()
                .map(converter::convertToReviewDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ReviewDto getReview(Long memberId, Long itemId) {
        Review review = reviewRepository.findByMemberIdAndItemId(memberId, itemId)
                .orElseThrow(() -> new CustomError(REVIEW_NOT_FOUND));
        return converter.convertToReviewDto(review);
    }
}
