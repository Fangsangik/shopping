package searching_program.search_product.domain;

import lombok.*;
import searching_program.search_product.type.Grade;
import searching_program.search_product.type.MemberStatus;
import searching_program.search_product.type.PaymentMethod;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder (toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<Item> items = new ArrayList<>();

    private String username;
    private int age;
    private String password;
    private String address;
    private LocalDateTime birth;
    private LocalDateTime createdAt;    // 엔티티 필드 이름
    private LocalDateTime deletedAt;    // 엔티티 필드 이름

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    private Grade grade;

    @Enumerated(EnumType.STRING)
    private MemberStatus memberStatus;

    public void deactivate() {
        this.memberStatus = MemberStatus.UN_ACTIVE;
    }

    public void reactivate() {
        this.memberStatus = MemberStatus.ACTIVE;
    }
}