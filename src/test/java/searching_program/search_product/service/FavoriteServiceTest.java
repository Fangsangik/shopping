package searching_program.search_product.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import searching_program.search_product.domain.Category;
import searching_program.search_product.domain.Item;
import searching_program.search_product.domain.ItemFavorite;
import searching_program.search_product.domain.Member;
import searching_program.search_product.dto.CategoryDto;
import searching_program.search_product.dto.DtoEntityConverter;
import searching_program.search_product.dto.ItemDto;
import searching_program.search_product.dto.MemberDto;
import searching_program.search_product.repository.CategoryRepository;
import searching_program.search_product.repository.ItemFavoriteRepository;
import searching_program.search_product.repository.ItemRepository;
import searching_program.search_product.repository.MemberRepository;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FavoriteServiceTest {

    @Autowired
    private FavoriteService favoriteService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private DtoEntityConverter converter;

    @Autowired
    private ItemFavoriteRepository itemFavoriteRepository;
    @Autowired
    private CategoryRepository categoryRepository;

    private ItemDto itemDto;
    private MemberDto memberDto;
    private CategoryDto categoryDto;

    @BeforeEach
    void setUp() {
        Member member = Member.builder()
                .userId("hello") // userId 설정
                .username("Test User")
                .build();
        member = memberRepository.save(member);
        memberDto = converter.convertToMemberDto(member);

        Category category = Category.builder()
                .name("electronic")
                .build();
        category = categoryRepository.save(category);
        categoryDto = converter.convertToCategoryDto(category);

        Item item = Item.builder()
                .itemName("Test Item")
                .itemFavorites(new ArrayList<>())
                .category(category) // category 설정
                .build();
        item = itemRepository.save(item);
        itemDto = converter.convertToItemDto(item);

        ItemFavorite itemFavorite = ItemFavorite.builder()
                .member(member)
                .item(item)
                .build();
        itemFavoriteRepository.save(itemFavorite);
    }

    /**
     * ERROR!!
     * FavoriteItemRepository에서 ItemDto를 받아오고 잇었음
     */

    @Transactional
    @Test
    void addFavoritesItem() {
        List<ItemDto> favoriteItemsByMemberId = favoriteService.findFavoriteItemsByUserId("hello");

        // 리스트가 비어 있는지 여부 확인
        System.out.println("Favorite Items: " + favoriteItemsByMemberId);

        // 예상과 실제 값 확인
        assertFalse(favoriteItemsByMemberId.isEmpty(), "Favorite items should not be empty");

        assertEquals(1, favoriteItemsByMemberId.size());
        assertEquals("Test Item", favoriteItemsByMemberId.get(0).getItemName());
    }

    @Transactional
    @Test
    void removeFavorites() {

        // Then: 즐겨찾기에 아이템이 추가되었는지 확인
        List<ItemDto> favoriteItems = favoriteService.findFavoriteItemsByUserId(memberDto.getUserId());
        assertFalse(favoriteItems.isEmpty(), "아이템이 즐겨찾기에 추가되지 않았습니다.");

        // When: 즐겨찾기에서 아이템 삭제
        favoriteService.removeFavorites(memberDto.getUserId(), itemDto.getId());

        // Then: 즐겨찾기에서 아이템이 제거되었는지 확인
        List<ItemDto> favoriteItemsAfterRemoval = favoriteService.findFavoriteItemsByUserId(memberDto.getUserId());
        assertTrue(favoriteItemsAfterRemoval.isEmpty(), "아이템이 즐겨찾기에서 제거되지 않았습니다.");
    }


    @Transactional
    @Test
    void findFavoriteItemsByUserId() {
        List<ItemFavorite> findUserId = itemFavoriteRepository.findByMemberUserId(memberDto.getUserId());

        assertFalse(findUserId.isEmpty(), "즐겨찾기 항목이 비어있지 않아야 합니다.");
        assertEquals(1, findUserId.size(), "즐겨찾기 항목의 수가 예상과 일치해야 합니다.");
        assertEquals(itemDto.getId(), findUserId.get(0).getItem().getId(), "조회된 아이템의 ID가 예상과 일치해야 합니다."); // itemDto.getItemId()에서 itemDto.getId()로 수정
        assertEquals(memberDto.getUserId(), findUserId.get(0).getMember().getUserId(), "조회된 유저의 ID가 예상과 일치해야 합니다.");
    }
}
