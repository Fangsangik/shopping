package searching_program.search_product.dto;

import lombok.*;
import searching_program.search_product.type.PaymentStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDto {
    private Long id;
    private Long orderId;
    private Double amount;
    private PaymentStatus status;
    private LocalDateTime paymentDate;
    private String couponCode;
}
