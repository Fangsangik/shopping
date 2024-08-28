package searching_program.search_product.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import searching_program.search_product.domain.Bucket;
import searching_program.search_product.domain.Item;
import searching_program.search_product.domain.Member;
import searching_program.search_product.dto.*;
import searching_program.search_product.error.CustomError;
import searching_program.search_product.repository.BucketRepository;
import searching_program.search_product.repository.CategoryRepository;
import searching_program.search_product.repository.ItemRepository;
import searching_program.search_product.repository.MemberRepository;
import searching_program.search_product.type.ItemStatus;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BucketServiceTest {

    @Autowired
    private BucketRepository bucketRepository;
    @Autowired
    private BucketService bucketService;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private DtoEntityConverter converter;
    @Autowired
    private CategoryRepository categoryRepository;

    private MemberDto memberDto;
    private ItemDto itemDto;
    private CategoryDto categoryDto;
    private BucketDto bucketDto;

    @BeforeEach
    void setUp() {
        Member member = memberRepository.save(Member.builder()
                .userId("testUser")
                .build());

        Item item = itemRepository.save(Item.builder()
                .itemName("testItem")
                .itemPrice(1000)
                .stock(50)
                .itemStatus(ItemStatus.AVAILABLE)
                .build());

        memberDto = MemberDto.builder()
                .id(member.getId())
                .userId(member.getUserId())
                .build();

        itemDto = ItemDto.builder()
                .id(item.getId())
                .itemName(item.getItemName())
                .itemPrice(item.getItemPrice())
                .stock(item.getStock())
                .build();
    }

    @Transactional
    @Test
    void test_findAll() {
        bucketService.addItemToBucket(memberDto.getId(), itemDto.getItemName(), 10);
        List<Bucket> findAll = bucketRepository.findAll();
        assertEquals(1, findAll.size(), "장바구니 항목의 크가가 예상과 다릅니다.");

        Bucket bucket = findAll.get(0);
        assertNotNull(bucket, "Bucket은 null이면 안됩니다.");
        assertEquals("testItem", bucket.getItem().getItemName(), "아이템 이름이 예상과 다릅니다.");
        assertEquals("testUser", bucket.getMember().getUserId(), "회원 ID가 예상과 다릅니다.");
        assertEquals(10000, bucket.getItemTotalPrice(), "총 가격이 예상과 다릅니다.");
        assertTrue(bucket.isSelected(), "장바구니 선택 상태가 예상과 다릅니다.");

    }

    @Transactional
    @Test
    void findByItemName() {
        bucketService.addItemToBucket(memberDto.getId(), itemDto.getItemName(), 10);
        List<Bucket> bucket = bucketRepository.findByItem_ItemName(itemDto.getItemName());

        assertThat(bucket).isNotNull();
        assertThat(bucket.get(0).getItem()).isNotNull();
        assertThat(bucket.get(0).getItem().getItemName()).isEqualTo(itemDto.getItemName());
    }

    @Transactional
    @Test
    void deleteBucketTest() {
        // given
        // 장바구니에 항목을 추가
        BucketDto addedBucket = bucketService.addItemToBucket(memberDto.getId(), itemDto.getItemName(), 10);

        // 장바구니에 항목이 추가되었는지 검증
        assertThat(addedBucket).isNotNull();
        assertThat(addedBucket.getItemName()).isEqualTo(itemDto.getItemName());
        assertThat(addedBucket.getQuantity()).isEqualTo(10);

        // when
        // 추가된 항목을 삭제
        Bucket bucket = bucketRepository.findById(addedBucket.getId()).orElseThrow(() ->
                new IllegalArgumentException("삭제할 장바구니 항목을 찾을 수 없습니다.")
        );
        bucketRepository.delete(bucket);

        // then
        // 삭제된 항목이 더 이상 존재하지 않는지 검증
        boolean exists = bucketRepository.existsById(addedBucket.getId());
        assertThat(exists).isFalse();
    }

    @Test
    void addItemToBucket() {
        int initialQuantity = 10;

        BucketDto addBucket = bucketService.addItemToBucket(memberDto.getId(), itemDto.getItemName(), initialQuantity);

        assertThat(addBucket).isNotNull();
        assertThat(addBucket.getItemName()).isEqualTo(itemDto.getItemName());
        assertThat(addBucket.getQuantity()).isEqualTo(initialQuantity);
        assertThat(addBucket.getItemTotalPrice()).isEqualTo(itemDto.getItemPrice() * initialQuantity);
    }

    @Test
    void addItemToBucketWithExistingItemTest() {
        int initialQuantity = 10;
        int additionalQuantity = 5;

        BucketDto addBucket = bucketService.addItemToBucket(memberDto.getId(), itemDto.getItemName(), initialQuantity);
        BucketDto updatedBucket = bucketService.addItemToBucket(memberDto.getId(), itemDto.getItemName(), additionalQuantity);

        assertThat(updatedBucket).isNotNull();
        assertThat(updatedBucket.getQuantity()).isEqualTo(initialQuantity + additionalQuantity);
        assertThat(updatedBucket.getItemTotalPrice()).isEqualTo(itemDto.getItemPrice() * (initialQuantity + additionalQuantity));

        // 데이터베이스에서 확인
        Bucket bucket = bucketRepository.findById(updatedBucket.getId()).orElseThrow();
        assertThat(bucket.getQuantity()).isEqualTo(initialQuantity + additionalQuantity);
        assertThat(bucket.getItemTotalPrice()).isEqualTo(itemDto.getItemPrice() * (initialQuantity + additionalQuantity));

    }

    @Test
    void validateBucketItemsWithUnavailableItemTest() {
        // given
        BucketDto addedBucket = bucketService.addItemToBucket(memberDto.getId(), itemDto.getItemName(), 10);

        // 상태 변경 시뮬레이션
        Item item = itemRepository.findById(itemDto.getId()).orElseThrow();
        item.setItemStatus(ItemStatus.OUT_OF_STOCK);
        itemRepository.save(item);

        // when & then
        CustomError exception = assertThrows(CustomError.class, () ->
                bucketService.validateBucketItems(memberDto.getId())
        );

        assertThat(exception.getMessage()).contains("더이상 판매되지 않는 아이탬 입니다.");
    }

    @Test
    void validateBucketItemTest() {
        bucketService.addItemToBucket(memberDto.getId(), itemDto.getItemName(), 10);
        bucketService.validateBucketItems(memberDto.getId());
    }

    @Test
    void priceChangeTest() {
        BucketDto addItemToBucket = bucketService.addItemToBucket(memberDto.getId(), itemDto.getItemName(), 10);

        Item item = itemRepository.findById(itemDto.getId()).orElseThrow(() -> new IllegalArgumentException("아이탬을 찾을 수 없습니다."));
        item.setItemPrice(2000);
        itemRepository.save(item);

        CustomError exception = assertThrows(CustomError.class, () ->
                bucketService.validateBucketItems(memberDto.getId())
        );

        assertThat(exception.getMessage()).contains("아이탬 가격이 변동 되었습니다.");
    }
}