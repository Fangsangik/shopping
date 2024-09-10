package searching_program.search_product.domain;

import jakarta.persistence.*;
import lombok.*;
import searching_program.search_product.type.ItemStatus;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tblItem")
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String itemName;
    private String link;
    private String image;
    private int itemPrice; // ItemDto의 price와 통일성을 위해 itemPrice 사용
    private int lowPrice;
    private int maxPrice;
    private int myPrice;
    private int stock;
    private Double discountedPrice;

    @Enumerated(EnumType.STRING)
    private ItemStatus itemStatus;

    //Item이 여러 bucket을 소유 할 수 있음
    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Bucket> buckets = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemFavorite> itemFavorites = new ArrayList<>();

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>();

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Promotion> promotions = new ArrayList<>();

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Orders> orders = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    public void updateStatus(ItemStatus itemStatus) {
        this.itemStatus = itemStatus;
    }
}

