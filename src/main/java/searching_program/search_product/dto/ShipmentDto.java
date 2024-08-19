package searching_program.search_product.dto;

import lombok.*;
import searching_program.search_product.domain.Order;
import searching_program.search_product.type.ShipmentStatus;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentDto {

    private Long id;
    private String curLocation;
    private ShipmentStatus shipmentStatus;
    private LocalDateTime estimatedDeliveryDate;
}
