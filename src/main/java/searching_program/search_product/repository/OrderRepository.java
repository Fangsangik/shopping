package searching_program.search_product.repository;

import org.aspectj.weaver.ast.Or;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import searching_program.search_product.domain.Order;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByItemName(String itemDto, Pageable pageable);

    Page<Order> findByMemberId (Long memberId, Pageable pageable);

}
