package searching_program.search_product.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDto {

    private Long id;
    private Long itemId;
    private int quantity;
    private int price;
}

