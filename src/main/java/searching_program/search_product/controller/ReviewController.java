package searching_program.search_product.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searching_program.search_product.dto.ReviewDto;
import searching_program.search_product.error.CustomError;
import searching_program.search_product.service.ReviewService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/review")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    @PostMapping("/addReview")
    public ResponseEntity<String> addReview(
            @RequestParam Long orderId,
            @RequestParam Long memberId,
            @RequestParam Long itemId,
            @RequestBody String reviewText,
            @RequestParam int rate) {
        try {
            reviewService.addReview(orderId, memberId, itemId, reviewText, rate);
            log.info("리뷰가 성공적으로 추가되었습니다: 주문 ID = {}, 회원 ID = {}, 아이템 ID = {}", orderId, memberId, itemId);
            return ResponseEntity.ok("리뷰가 성공적으로 추가되었습니다.");
        } catch (CustomError e) {
            log.error("리뷰 추가 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            log.error("예기치 않은 오류 발생: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("리뷰 추가 중 오류가 발생했습니다.");
        }
    }

    @PostMapping("/removeReview")
    public ResponseEntity<String> removeReview(
            @RequestParam Long memberId,
            @RequestParam Long itemId) {
        try {
            reviewService.removeReview(memberId, itemId);
            log.info("리뷰가 성공적으로 삭제되었습니다: 회원 ID = {}, 아이템 ID = {}", memberId, itemId);
            return ResponseEntity.ok("리뷰가 성공적으로 삭제되었습니다.");
        } catch (CustomError e) {
            log.error("리뷰 삭제 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            log.error("예기치 않은 오류 발생: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("리뷰 삭제 중 오류가 발생했습니다.");
        }
    }

    @PostMapping("/edit")
    public ResponseEntity<String> editReview(
            @RequestParam Long memberId,
            @RequestParam Long itemId,
            @RequestBody String newReviewText,
            @RequestParam int newRate) {
        try {
            reviewService.editReview(memberId, itemId, newReviewText, newRate);
            log.info("리뷰가 성공적으로 수정되었습니다: 회원 ID = {}, 아이템 ID = {}, 새로운 텍스트 = {}, 평가 = {}", memberId, itemId, newReviewText, newRate);
            return ResponseEntity.ok("리뷰가 성공적으로 수정되었습니다.");
        } catch (CustomError e) {
            log.error("리뷰 수정 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            log.error("예기치 않은 오류 발생: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("리뷰 수정 중 오류가 발생했습니다.");
        }
    }

    @GetMapping("/review/{itemId}")
    public ResponseEntity<List<ReviewDto>> reviewItemId(@PathVariable Long itemId) {
        try {
            List<ReviewDto> reviewsByItemId = reviewService.getReviewsByItemId(itemId);
            log.info("아이템 ID {}에 대한 리뷰 목록을 성공적으로 조회했습니다.", itemId);
            return ResponseEntity.ok(reviewsByItemId);
        } catch (Exception e) {
            log.error("리뷰 조회 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/getReview/{memberId}/{itemId}")
    public ResponseEntity<ReviewDto> getReview(@PathVariable Long memberId, @PathVariable Long itemId) {
        try {
            ReviewDto review = reviewService.getReview(memberId, itemId);
            log.info("회원 ID {}와 아이템 ID {}에 대한 리뷰를 성공적으로 조회했습니다.", memberId, itemId);
            return ResponseEntity.ok(review);
        } catch (CustomError e) {
            log.error("리뷰 조회 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            log.error("예기치 않은 오류 발생: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
