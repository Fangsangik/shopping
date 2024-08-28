package searching_program.search_product.domain;

import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tblPromotion")
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Item item;

    private Long discountRate;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String couponCode; // 고유 번호(쿠폰 코드) 추가

}
