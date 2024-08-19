package searching_program.search_product.dto;

import lombok.*;
import searching_program.search_product.domain.OrderItem;
import searching_program.search_product.domain.Shipment;
import searching_program.search_product.type.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {

    private Long id;
    private Long memberId;
    private LocalDateTime orderDate;
    private OrderStatus orderStatus;
    private List<OrderItemDto> orderItems;
}
