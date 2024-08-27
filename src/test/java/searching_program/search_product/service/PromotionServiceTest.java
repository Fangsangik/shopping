package searching_program.search_product.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import searching_program.search_product.domain.Category;
import searching_program.search_product.domain.Item;
import searching_program.search_product.domain.Promotion;
import searching_program.search_product.dto.CategoryDto;
import searching_program.search_product.dto.DtoEntityConverter;
import searching_program.search_product.dto.ItemDto;
import searching_program.search_product.dto.PromotionDto;
import searching_program.search_product.error.CustomError;
import searching_program.search_product.repository.CategoryRepository;
import searching_program.search_product.repository.ItemRepository;
import searching_program.search_product.repository.PromotionRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class PromotionServiceTest {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private PromotionService promotionService;

    @Autowired
    private PromotionRepository promotionRepository;

    @Autowired
    private DtoEntityConverter converter;

    @Autowired
    private CategoryRepository categoryRepository;

    private ItemDto itemDto;
    private CategoryDto categoryDto;

    @BeforeEach
    void setUp() {
        Category category = categoryRepository.save(Category.builder()
                .name("Test")
                .build());

        categoryDto = converter.convertToCategoryDto(category);


        Item item = itemRepository.save(Item.builder()
                .itemName("testItem")
                .category(category)
                .itemPrice(1000)
                .build());

        itemDto = converter.convertToItemDto(item);


        Promotion promotion = Promotion.builder()
                .item(item)
                .discountRate(10L)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(1))
                .build();
        promotionRepository.save(promotion);
    }

    @Test
    void applyPromotionTest() {
        LocalDateTime startDate = LocalDateTime.now().plusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(10);

        promotionService.applyPromotion(itemDto.getId(), 10L, startDate, endDate);
        boolean promotionExists = promotionRepository
                .existsByItemAndStartDateLessThanEqualAndEndDateGreaterThanEqual(converter.convertToItemEntity(itemDto), endDate, startDate);

        assertTrue(promotionExists, "프로모션이 성공적으로 작동해야 합니다.");
    }

    @Test
    void applyPromotionFail() {
        LocalDateTime startDate = LocalDateTime.now().plusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(10);

        assertThrows(CustomError.class, () -> {
            promotionService.applyPromotion(itemDto.getId(), -10L, startDate, endDate);
        }, "음수 할인율을 설정하면 안됩니다.");


        assertThrows(CustomError.class, () -> {
            promotionService.applyPromotion(itemDto.getId(), 110L, startDate, endDate);
        }, "100%를 넘어서는 할인율을 설정하면 안됩니다.");
    }

    @Test
    void applyDuplicatePromotionFail() {
        LocalDateTime startDate = LocalDateTime.now().plusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(10);

        assertThrows(CustomError.class, () -> {
            promotionService.applyPromotion(itemDto.getId(), 20L, startDate, endDate);
        }, "중복된 프로모션 적용 시 예외가 발생해야 합니다.");
    }

    @Test
    void getItemsWithActivePromotions() {
        List<ItemDto> activePromotions = promotionService.getItemsWithActivePromotions();
        assertNotNull(activePromotions);
        assertEquals(1, activePromotions.size());
        assertEquals(itemDto.getId(), activePromotions.get(0).getId(), "조회된 아이템의 ID가 예상과 일치해야 합니다.");
    }
}