package searching_program.search_product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import searching_program.search_product.domain.Item;
import searching_program.search_product.domain.Promotion;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    @Query("SELECT p.item FROM Promotion p WHERE :now BETWEEN p.startDate AND p.endDate")
    List<Item> findItemsWithActivePromotions(@Param("now") LocalDateTime now);

    boolean existsByItemAndStartDateLessThanEqualAndEndDateGreaterThanEqual(Item item, LocalDateTime endDate, LocalDateTime startDate);
    Promotion findFirstByItemAndStartDateLessThanEqualAndEndDateGreaterThanEqual(Item item, LocalDateTime startDate, LocalDateTime endDate);
    List<Promotion> findByItem(Item item);

    @Query("SELECT p FROM Promotion p WHERE p.item.id = :itemId AND :currentDate BETWEEN p.startDate AND p.endDate")
    Optional<Promotion> findActivePromotionByItem(@Param("itemId") Long itemId, @Param("currentDate") LocalDateTime currentDate);

}
