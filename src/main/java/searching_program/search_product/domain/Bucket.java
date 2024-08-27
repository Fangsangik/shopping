package searching_program.search_product.domain;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

import static javax.persistence.GenerationType.IDENTITY;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "tblBucket")
public class Bucket {

    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "bucket_id")
    private Item item;

    //Member를 단방형 연관관계로 설정함으로써 복잡성 감소
    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    private int quantity;
    private LocalDateTime addedAt;
    private boolean isSelected;
    private int itemTotalPrice;

    public int calculateItemTotalPrice() {
        return quantity * item.getItemPrice();
    }

    public void updateQuantity(int newQuantity) {
        if (newQuantity > 0) {
            quantity = newQuantity;
        } else {
            throw new IllegalArgumentException("수량은 0보다 커야 합니다.");
        }
    }
}
