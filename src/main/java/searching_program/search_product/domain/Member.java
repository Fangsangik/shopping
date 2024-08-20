package searching_program.search_product.domain;

import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
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
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<Item> items = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<ItemFavorite> itemFavorites = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<Review> reviews = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<Orders> orders = new ArrayList<>();

    @Column(unique = true, nullable = false)
    private String userId; // 사용자가 입력하는 ID

    private String username;
    private int age;
    private String password;
    private String address;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime birth;

    private LocalDateTime createdAt;
    private LocalDateTime deletedAt;

    private boolean lock;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
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
