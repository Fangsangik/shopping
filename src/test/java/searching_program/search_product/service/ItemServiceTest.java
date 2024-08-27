package searching_program.search_product.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.data.domain.Page;
import searching_program.search_product.domain.Category;
import searching_program.search_product.domain.Item;
import searching_program.search_product.dto.CategoryDto;
import searching_program.search_product.dto.DtoEntityConverter;
import searching_program.search_product.dto.ItemDto;
import searching_program.search_product.repository.CategoryRepository;
import searching_program.search_product.repository.ItemRepository;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static searching_program.search_product.type.ItemStatus.AVAILABLE;
import static searching_program.search_product.type.ItemStatus.OUT_OF_STOCK;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;

@SpringBootTest
@EnableCaching
class ItemServiceTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private DtoEntityConverter converter;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CacheManager cacheManager;

    private ItemDto itemDto1;
    private ItemDto itemDto2;

    private CategoryDto categoryDto;

    @BeforeEach
    void setUp() {
        Category category = categoryRepository.save(Category.builder()
                .name("electronic")
                .build());
        categoryDto = converter.convertToCategoryDto(category);

        Item item1 = itemRepository.save(Item.builder()
                .itemName("macbook")
                .itemPrice(15000)  // int 사용
                .lowPrice(1000)    // int 사용
                .maxPrice(19000)   // int 사용
                .stock(20)
                .category(category)
                .build());

        itemDto1 = converter.convertToItemDto(item1);

        Item item2 = itemRepository.save(Item.builder()
                .itemName("airpods")
                .itemPrice(11000)  // int 사용
                .lowPrice(2000)    // int 사용
                .maxPrice(18000)   // int 사용
                .stock(20)
                .category(category)
                .build());

        itemDto2 = converter.convertToItemDto(item2);
    }

    @Transactional
    @Test
    void findByItemName() {
        List<ItemDto> byItemName1 = itemService.findByItemName(itemDto1.getItemName());
        List<ItemDto> byItemName2 = itemService.findByItemName(itemDto2.getItemName());

        assertEquals(1, byItemName1.size());
        assertEquals(1, byItemName2.size());
        assertEquals("mac", byItemName1.get(0).getItemName());
        assertEquals("air", byItemName2.get(0).getItemName());
    }

    @Transactional
    @Test
    void findByItemNames() {
        List<ItemDto> itemDtos = itemService.findByItemNames(itemDto1.getItemName(), itemDto2.getItemName());
        assertEquals(2, itemDtos.size());
    }

    @Transactional
    @Test
    void searchItems() {
        String itemName = "mac";
        int minPrice = 1000;
        int maxPrice = 20000;
        String categoryName = "electronic";
        int pageNum = 0;
        int pageSize = 10;

        //이름 별로 검색
        Page<Item> rstName = itemService.searchItems(itemName, -1, -1, null, 0, 10);
        assertEquals(1, rstName.getTotalElements());

        //가격별로 검색
        Page<Item> rstPrice = itemService.searchItems(null, minPrice, maxPrice, null, 0, 10);
        assertEquals(2, rstPrice.getTotalElements());

        // 카테고리 이름으로 검색
        Page<Item> resultsByCategory = itemService.searchItems(null, -1, -1, categoryName, 0, 10);
        assertEquals(2, resultsByCategory.getTotalElements());
    }

    @Test
    void findByItemPrice() {
        int itemPrice = 15000;
        int pageNum = 0;
        int pageSize = 10;

        Page<ItemDto> rstPrice = itemService.findByItemPrice(itemPrice, 0);

        assertEquals(1, rstPrice.getTotalElements());
        assertEquals(15000, itemPrice);
    }

    /**
     * TODO : EMAIL 인증 보안 키 설정하기
     */

//    @Test
//    void checkNotification() {
//        Category category = categoryRepository.save(Category.builder().name("Test Category").build());

//        Item item1 = itemRepository.save(Item.builder()
//                .itemName("Item 1")
//                .itemPrice(10000)
//                .stock(5)  // 재고가 임계값 이하
//                .itemStatus(AVAILABLE)
//                .category(category)
//                .build());

//        Item item2 = itemRepository.save(Item.builder()
//                .itemName("Item 2")
//                .itemPrice(15000)
//                .stock(12)  // 재고가 임계값 이상
//                .itemStatus(AVAILABLE)
//                .category(category)
//                .build());

//        itemService.checkAndNotification();

//        Item updatedItem1 = itemRepository.findById(item1.getId()).orElseThrow();
//        assertEquals(OUT_OF_STOCK, updatedItem1.getItemStatus());

//        Item updatedItem2 = itemRepository.findById(item2.getId()).orElseThrow();
//        assertEquals(AVAILABLE, updatedItem2.getItemStatus());
//    }
}
