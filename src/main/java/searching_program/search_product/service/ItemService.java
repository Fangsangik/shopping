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
import searching_program.search_product.domain.*;
import searching_program.search_product.dto.CategoryDto;
import searching_program.search_product.dto.DtoEntityConverter;
import searching_program.search_product.dto.ItemDto;
import searching_program.search_product.error.CustomError;
import searching_program.search_product.repository.CategoryRepository;
import searching_program.search_product.repository.ItemRepository;
import searching_program.search_product.repository.MemberRepository;
import searching_program.search_product.repository.PromotionRepository;
import searching_program.search_product.service.notification.NotificationService;
import searching_program.search_product.type.ErrorCode;
import searching_program.search_product.type.ItemStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static searching_program.search_product.type.ErrorCode.ITEM_NOT_FOUND;
import static searching_program.search_product.type.ErrorCode.PROMOTION_MUST_NOT_OVER_THAN_HUNDRED;
import static searching_program.search_product.type.ItemStatus.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final DtoEntityConverter converter;
    private final CategoryService categoryService;
    private final NotificationService notificationService;
    private final CategoryRepository categoryRepository;
    private final PromotionRepository promotionRepository;

    @Transactional
    public ItemDto createItem(ItemDto itemDto) {
        // 중복된 아이템 이름이 있는지 검증 (필요한 경우)
        if (itemRepository.existsByItemName(itemDto.getItemName())) {
            throw new CustomError(ErrorCode.ITEM_DUPLICATE);
        }

        // 카테고리 이름 가져오기
        String categoryName = null;
        if (itemDto.getCategoryDto() != null) {
            categoryName = itemDto.getCategoryDto().getName();
        } else {
            throw new CustomError(ErrorCode.CATEGORY_NOT_FOUND);
        }

        // 카테고리 존재 여부 확인 및 처리
        CategoryDto categoryDto = categoryService.findByName(categoryName);
        if (categoryDto == null) {
            throw new CustomError(ErrorCode.CATEGORY_NOT_FOUND);
        }


        // DTO를 엔티티로 변환
        Item item = converter.convertToItemEntity(itemDto);

        // 아이템 저장
        Item savedItem = itemRepository.save(item);

        // 저장된 엔티티를 DTO로 변환하여 반환
        return converter.convertToItemDto(savedItem);
    }

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

    @Transactional(readOnly = true)
    public ItemDto getItemWithPromotion(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new CustomError(ITEM_NOT_FOUND));

        Promotion activePromotion = promotionRepository.findFirstByItemAndStartDateLessThanEqualAndEndDateGreaterThanEqual
                (item, LocalDateTime.now(), LocalDateTime.now());

        double finalPrice = item.getItemPrice();
        if (activePromotion != null) {
            // 프로모션이 적용된 할인된 가격 계산
            finalPrice = calculateDiscountedPrice(item.getId(), activePromotion.getDiscountRate());
        }

        ItemDto itemDto = converter.convertToItemDto(item);
        itemDto.setDiscountedPrice(finalPrice); // 할인된 가격을 DTO에 설정

        return itemDto;
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

    // 할인된 가격 계산 메서드
    public double calculateDiscountedPrice(Long itemId, Long discountRate) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new CustomError(ITEM_NOT_FOUND));

        if (discountRate == null || discountRate <= 0) {
            return item.getItemPrice();
        }

        if (discountRate > 100) {
            throw new CustomError(PROMOTION_MUST_NOT_OVER_THAN_HUNDRED);
        }

        double discountMultiplier = (100.0 - discountRate) / 100.0;
        return item.getItemPrice() * discountMultiplier;
    }
}
