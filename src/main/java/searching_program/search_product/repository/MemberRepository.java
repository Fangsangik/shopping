package searching_program.search_product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import searching_program.search_product.domain.Member;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    List<Member> findByUsernameContaining(String username);
    List<Member> findByAgeGreaterThan(int age);
    Optional<Member> findByUsername(String username);}
