package searching_program.search_product.dto;

import lombok.*;

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
    private int price;
    private int lowPrice;
    private int maxPrice;
    private int myPrice;
    private int stock;
}
