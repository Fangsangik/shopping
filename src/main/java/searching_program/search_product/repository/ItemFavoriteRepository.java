package searching_program.search_product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import searching_program.search_product.domain.Item;
import searching_program.search_product.domain.ItemFavorite;

import java.util.List;

public interface ItemFavoriteRepository extends JpaRepository<ItemFavorite, Long> {
    void deleteByUserIdAndItemId(Long memberId, Long itemId);

    List<Item> findItemsByMemberId(Long memberId);
}
