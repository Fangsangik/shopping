package searching_program.search_product.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromotionDto {
    private Long id;
    private Long itemId;
    private Long discountRate;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
