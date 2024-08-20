package searching_program.search_product.dto;

import lombok.*;
import searching_program.search_product.type.OrderStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusHistoryDto {
    private OrderStatus status;
    private LocalDateTime timestamp;
}
