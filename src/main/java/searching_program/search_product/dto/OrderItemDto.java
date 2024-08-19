package searching_program.search_product.dto;

import lombok.*;
import searching_program.search_product.domain.Order;

import java.util.List;

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
