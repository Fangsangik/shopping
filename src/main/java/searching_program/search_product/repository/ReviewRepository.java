package searching_program.search_product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import searching_program.search_product.domain.Item;
import searching_program.search_product.domain.Review;
import searching_program.search_product.type.ItemStatus;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Optional<Review> findByMemberIdAndItemId(Long memberId, Long itemId);

    List<Review> findByItemId(Long itemId);
}
