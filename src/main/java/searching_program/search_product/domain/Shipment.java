package searching_program.search_product.domain;

import jakarta.persistence.*;
import lombok.*;
import searching_program.search_product.type.ShipmentStatus;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tblShipment")
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "order_id")
    private Orders order;

    private String curLocation;
    private ShipmentStatus shipmentStatus;
    private LocalDateTime estimatedDeliveryDate;
}
