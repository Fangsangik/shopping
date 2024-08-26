package searching_program.search_product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import searching_program.search_product.domain.Payment;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByMember_Id(Long memberId);
}
