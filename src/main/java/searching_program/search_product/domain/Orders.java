package searching_program.search_product.domain;


import lombok.*;
import searching_program.search_product.type.OrderStatus;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Orders {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne
    @JoinColumn(name = "item_id")
    private Item item;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderStatusHistory> statusHistory = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Payment> payments = new ArrayList<>();

    private LocalDateTime orderDate;
    private LocalDateTime createdDate; // 추가: 주문 생성 날짜 및 시간

    @Enumerated(EnumType.STRING)
    private OrderStatus status; // OrderDto의 orderStatus와 일치하도록 변경

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Shipment shipment;

    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    public void changeStatus(OrderStatus newStatus) {
        if (this.status != newStatus) {
            this.status = newStatus;
            addStatusHistory(newStatus);
        }
    }

    private void addStatusHistory(OrderStatus newStatus) {
        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(this)
                .status(newStatus)
                .timestamp(LocalDateTime.now())
                .build();
        this.statusHistory.add(history);
    }
}
