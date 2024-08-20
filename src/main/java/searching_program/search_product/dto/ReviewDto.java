package searching_program.search_product.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDto {
    private Long id;
    private String userId;
    private Long itemId;
    private String reviewText;
    private int rate;
}
