package searching_program.search_product.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searching_program.search_product.domain.Category;
import searching_program.search_product.domain.Item;
import searching_program.search_product.domain.ItemFavorite;
import searching_program.search_product.domain.Member;
import searching_program.search_product.dto.DtoEntityConverter;
import searching_program.search_product.dto.ItemDto;
import searching_program.search_product.repository.CategoryRepository;
import searching_program.search_product.repository.ItemRepository;
import searching_program.search_product.repository.MemberRepository;
import searching_program.search_product.service.notification.NotificationService;
import searching_program.search_product.type.ItemStatus;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static searching_program.search_product.type.ItemStatus.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final DtoEntityConverter converter;
    private final NotificationService notificationService;
    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    @Cacheable("itemsByName") //자주 조회되는 데이터를 캐싱 -> DB에 대한 요청을 줄일 수 있음
    public List<ItemDto> findByItemName(String itemName) {
        List<Item> items = itemRepository.findByItemName(itemName);
        return items.stream()
                .map(converter::convertToItemDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Cacheable("itemsByNames") //자주 조회되는 데이터를 캐싱 -> DB에 대한 요청을 줄일 수 있음
    public List<ItemDto> findByItemNames(String itemName1, String itemName2) {
        List<Item> items = itemRepository
                .findByItemNameContainingOrItemNameContaining
                        (itemName1, itemName2);
        return items.stream()
                .map(converter::convertToItemDto)
                .collect(Collectors.toList());
    }

    /**
     * Specification -> JPA에서 동적 쿼리를 작성할 수 있도록 지원하는 인터페이스
     * 동적쿼리 배운 다음 동적 쿼리로 refactoring 해보
     */
    @Transactional(readOnly = true)
    public Page<Item> searchItems(String itemName, int minPrice, int maxPrice, String categoryName, int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("itemPrice").ascending());

        if (itemName != null) {
            return itemRepository.findByItemNameContaining(itemName, pageable);
        } else if (minPrice >= 0 && maxPrice >= 0) {
            return itemRepository.findByItemPriceBetween(minPrice, maxPrice, pageable);
        } else if (categoryName != null) {
            Category category = categoryRepository.findByName(categoryName)
                    .orElseThrow(() -> new IllegalArgumentException("Category not found: " + categoryName));
            return itemRepository.findByCategory(category, pageable);
        }

        return Page.empty(pageable);
    }

    @Transactional(readOnly = true)
    public Page<ItemDto> findByItemPrice(int price, int pageNumber) {
        if (pageNumber < 0) {
            log.warn("음수 페이지 번호가 입력되었습니다. 첫 번째 페이지를 반환합니다.");
            pageNumber = 0;
        }

        Pageable pageable = PageRequest.of(pageNumber, 5, Sort.by("itemPrice").ascending());
        Page<Item> itemPrice = itemRepository.findByItemPrice(price, pageable);
        return itemPrice.map(converter::convertToItemDto);
    }

    @Transactional(readOnly = true)
    public Page<ItemDto> findByItemPriceRange(int minPrice, int maxPrice, int pageNumber) {
        if (pageNumber < 0) {
            log.warn("음수 페이지 번호가 입력되었습니다. 첫 번째 페이지를 반환합니다.");
            pageNumber = 0;
        }
        Pageable pageable = PageRequest.of(pageNumber, 5, Sort.by("itemPrice").ascending());
        Page<Item> items = itemRepository.findByItemPriceBetween(minPrice, maxPrice, pageable);
        return items.map(converter::convertToItemDto);
    }

    @Transactional
    public void checkAndNotification() {
        int stockThreshold = 10; //재고 임계값 설정
        List<Item> alarm = itemRepository.findByStockLessThanEqualAndItemStatus(stockThreshold, AVAILABLE);
        for (Item item : alarm) {
            item.updateStatus(OUT_OF_STOCK);
            notificationService.sendLowStockAlert(item);
        }
    }
}
