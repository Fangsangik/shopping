package searching_program.search_product.domain;

import lombok.*;
import searching_program.search_product.type.ItemStatus;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
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

    @Enumerated(EnumType.STRING)
    private ItemStatus itemStatus;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemFavorite> itemFavorites = new ArrayList<>();

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL)
    private List<Review> reviews = new ArrayList<>();

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL)
    private List<Promotion> promotions = new ArrayList<>();

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL)
    private List<Orders> orders = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    public void updateStatus(ItemStatus itemStatus) {
        this.itemStatus = itemStatus;
    }
}

