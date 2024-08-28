package searching_program.search_product.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class PromotionRequest {
    private Long itemId;
    private Long discountRate;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String couponCode; // 고유 번호(쿠폰 코드) 추가
}
