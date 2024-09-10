package searching_program.search_product.domain;

import jakarta.persistence.*;
import lombok.*;
import searching_program.search_product.type.OrderStatus;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tblOrderStatusHistory")
public class OrderStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Orders order;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    private LocalDateTime timestamp;
}

