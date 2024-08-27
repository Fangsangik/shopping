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
import searching_program.search_product.error.CustomError;
import searching_program.search_product.repository.CategoryRepository;
import searching_program.search_product.repository.ItemRepository;
import searching_program.search_product.repository.MemberRepository;
import searching_program.search_product.service.notification.NotificationService;
import searching_program.search_product.type.ErrorCode;
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

    @Transactional(readOnly = true)
    public ItemDto findById(Long itemId) {
        Item findItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new CustomError(ErrorCode.ITEM_NOT_FOUND));
        return converter.convertToItemDto(findItem);
    }

    /**
     * TODO :
     * Specification -> JPA에서 동적 쿼리를 작성할 수 있도록 지원하는 인터페이스
     * 동적쿼리 배운 다음 동적 쿼리로 refactoring 해보
     */
    @Transactional(readOnly = true)
    public Page<Item> searchItems(String itemName, int minPrice, int maxPrice, String categoryName, int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("itemPrice").ascending());

        if (itemName != null) {
            return itemRepository.findByItemNameContaining(itemName, pageable);
        }

        if (minPrice >= 0 && maxPrice >= 0) {
            return itemRepository.findByItemPriceBetween(minPrice, maxPrice, pageable);
        }

        if (categoryName != null && !categoryName.isEmpty()) {
            Category category = categoryRepository.findByName(categoryName)
                    .orElseThrow(() -> new CustomError(ErrorCode.CATEGORY_NOT_FOUND));
            return itemRepository.findByCategory(category, pageable);
        }

        return Page.empty(pageable);
    }

    @Transactional(readOnly = true)
    public Page<ItemDto> findByItemPrice(Integer price, int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("itemPrice").ascending());

        Page<Item> itemPage;
        if (price != null) {
            itemPage = itemRepository.findByItemPrice(price, pageable);
        } else {
            itemPage = itemRepository.findAll(pageable); // price 파라미터가 없는 경우 모든 아이템 검색
        }

        return itemPage.map(converter::convertToItemDto);
    }

    @Transactional(readOnly = true)
    public Page<ItemDto> findByItemPriceRange(int minPrice, int maxPrice, int pageNumber, int pageSize) {
        if (pageNumber < 0) {
            log.warn("음수 페이지 번호가 입력되었습니다. 첫 번째 페이지를 반환합니다.");
            pageNumber = 0;
        }

        if (pageSize <= 0) {
            log.warn("페이지 크기가 0 이하로 설정되었습니다. 기본값 5를 사용합니다.");
            pageSize = 5; // 기본값 설정
        }

        // 가격 범위 유효성 검사
        if (minPrice > maxPrice) {
            log.warn("최소 가격이 최대 가격보다 큽니다. minPrice: {}, maxPrice: {}", minPrice, maxPrice);
            return Page.empty();
        }

        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("itemPrice").ascending());
        Page<Item> items = itemRepository.findByItemPriceBetween(minPrice, maxPrice, pageable);
        return items.map(converter::convertToItemDto);
    }

    @Transactional
    public void checkAndNotification(int stockThreshold) {
        List<Item> alarm = itemRepository.findByStockLessThanEqualAndItemStatus(stockThreshold, ItemStatus.AVAILABLE);

        for (Item item : alarm) {
            item.updateStatus(ItemStatus.OUT_OF_STOCK);  // 재고 상태 업데이트
            notificationService.sendLowStockAlert(item); // 알림 발송
        }
    }
}
