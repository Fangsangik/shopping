package searching_program.search_product.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemFavoriteDto {
    private Long id;
    private Long memberId;
    private Long itemId;
}
