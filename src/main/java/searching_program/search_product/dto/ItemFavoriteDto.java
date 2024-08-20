package searching_program.search_product.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemFavoriteDto {
    private Long id;
    private Long memberId; // 엔티티의 member 필드와 매핑
    private Long itemId;   // 엔티티의 item 필드와 매핑
}
