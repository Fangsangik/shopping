package searching_program.search_product.dto;

import lombok.*;
import searching_program.search_product.domain.Category;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ItemDto {

    private Long id;
    private String itemName;
    private String link;
    private String image;
    private int itemPrice; // 엔티티의 itemPrice와 일치하도록 변경
    private int lowPrice;
    private int maxPrice;
    private int myPrice;
    private int stock;
    private CategoryDto categoryDto;
    private Double discountedPrice;
}

