package searching_program.search_product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import searching_program.search_product.domain.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
