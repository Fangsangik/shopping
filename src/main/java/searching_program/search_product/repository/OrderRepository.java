package searching_program.search_product.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import searching_program.search_product.domain.Orders;

import java.util.List;

public interface OrderRepository extends JpaRepository<Orders, Long> {

    Page<Orders> findByItem_ItemName(String itemDto, Pageable pageable);

    Page<Orders> findByMemberUserId (String userId, Pageable pageable);

    Page<Orders> findByItem_ItemNameContaining(String itemName, Pageable pageable);

}
