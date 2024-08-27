package searching_program.search_product.dto;

import lombok.*;
import searching_program.search_product.domain.OrderStatusHistory;
import searching_program.search_product.type.OrderStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {

    private Long id;
    private String userId;
    private LocalDateTime orderDate;
    private OrderStatus status;
    private double totalAmount;
    private LocalDateTime createdDate; // 추가: 주문 생성 날짜 및 시간
    private List<OrderStatusHistoryDto> orderStatusHistories = new ArrayList<>();
    private List<OrderItemDto> orderItems = new ArrayList<>();
}
