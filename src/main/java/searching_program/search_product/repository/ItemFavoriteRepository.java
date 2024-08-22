package searching_program.search_product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import searching_program.search_product.domain.Item;
import searching_program.search_product.domain.ItemFavorite;

import java.util.List;

public interface ItemFavoriteRepository extends JpaRepository<ItemFavorite, Long> {
    void deleteByMemberUserIdAndItemId(String userId, Long itemId);
    List<ItemFavorite> findByMemberUserId(@Param("userId") String userId);
}
