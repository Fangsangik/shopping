package searching_program.search_product.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BucketDto {
    private Long id;
    private String itemName;
    private Long memberId;
    private int quantity;
    private LocalDateTime addedAt;
    private boolean isSelected;
    private int itemTotalPrice;
}
