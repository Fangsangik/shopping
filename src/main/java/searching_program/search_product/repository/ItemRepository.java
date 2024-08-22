package searching_program.search_product.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import searching_program.search_product.domain.Category;
import searching_program.search_product.domain.Item;
import org.springframework.data.domain.Pageable;
import searching_program.search_product.type.ItemStatus;

import java.math.BigDecimal;
import java.util.List;

/*
Repository에선 domain 값 사용
DTO를 반환하도록 만들면 서비스 레이어나 프레젠테이션 계층에 대한 의존성을 갖음
따라서 유지보수성 감소
 */
public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findByItemName(String itemName);

    Page<Item> findByItemNameContaining(String itemName, Pageable pageable);

    Page<Item> findByCategory(Category category, Pageable pageable);
    Page<Item> findByItemPrice(int price, Pageable pageable);

    Page<Item> findByItemPriceBetween(int minPrice, int maxPrice, Pageable pageable);

    List<Item> findByStockLessThanEqualAndItemStatus(int stock, ItemStatus itemStatus);

    List<Item> findByItemNameContainingOrItemNameContaining(String itemName1, String itemName2);
}
