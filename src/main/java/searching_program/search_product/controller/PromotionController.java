package searching_program.search_product.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import searching_program.search_product.dto.ItemDto;
import searching_program.search_product.error.CustomError;
import searching_program.search_product.service.PromotionService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("promotion")
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionService promotionService;

    @PostMapping("/apply")
    public ResponseEntity<String> applyPromotion(@RequestBody PromotionRequest promotionRequest) {
        try {
            promotionService.applyPromotion(
                    promotionRequest.getItemId(),
                    promotionRequest.getCouponCode()
            );
            log.info("아이탬 ID {}에 대해 프로모션이 성공적으로 적용되었습니다.", promotionRequest.getItemId());
            return ResponseEntity.ok("프로모션이 성공적으로 적용되었습니다");
        } catch (CustomError e) {
            log.error("프로모션 적용 중 오류 발생 : {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            log.error("서버 error ; {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/calculateDiscount")
    public ResponseEntity<Double> calculateDiscountedPrice(
            @RequestParam Long itemId,
            @RequestParam Long discountRate) {
        try {
            double discountedPrice = promotionService.calculateDiscountedPrice(itemId, discountRate);
            log.info("아이템 ID {}의 할인된 가격을 계산했습니다: {}", itemId, discountedPrice);
            return ResponseEntity.ok(discountedPrice);
        } catch (CustomError e) {
            log.error("할인된 가격 계산 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            log.error("예기치 않은 오류 발생: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/create")
    public ResponseEntity<String> createPromotion(@RequestBody PromotionRequest promotionRequest) {
        try {
            promotionService.createPromotion(
                    promotionRequest.getItemId(),
                    promotionRequest.getDiscountRate(),
                    promotionRequest.getStartDate(),
                    promotionRequest.getEndDate()
            );
            log.info("아이탬 ID {}에 대해 프로모션이 성공적으로 생성 및 적용되었습니다.", promotionRequest.getItemId());
            return ResponseEntity.ok("프로모션이 성공적으로 생성 및 적용되었습니다");
        } catch (CustomError e) {
            log.error("프로모션 생성 중 오류 발생 : {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            log.error("서버 error ; {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/getActivePromotion")
    public ResponseEntity<List<ItemDto>> activePromotions() {
        try {
            List<ItemDto> itemsWithActivePromotions = promotionService.getItemsWithActivePromotions();
            return ResponseEntity.ok(itemsWithActivePromotions);
        } catch (CustomError e) {
            log.error("해당 프로모션이 적용되지 않았습니다: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null); // 적절한 HTTP 응답 코드 반환
        } catch (Exception e) {
            log.error("예기치 않은 오류 발생: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
