package searching_program.search_product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import searching_program.search_product.domain.Bucket;
import searching_program.search_product.domain.Item;
import searching_program.search_product.domain.Member;

import java.util.List;

public interface BucketRepository extends JpaRepository<Bucket, Long> {
    Bucket findByItem_ItemName(String itemName);

    List<Bucket> findByMemberId(Long memberId);

    Bucket findByMemberAndItem(Member member, Item item);
}
