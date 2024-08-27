package searching_program.search_product.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searching_program.search_product.domain.Item;
import searching_program.search_product.domain.Promotion;
import searching_program.search_product.dto.DtoEntityConverter;
import searching_program.search_product.dto.ItemDto;
import searching_program.search_product.error.CustomError;
import searching_program.search_product.repository.ItemRepository;
import searching_program.search_product.repository.PromotionRepository;
import searching_program.search_product.type.ErrorCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static searching_program.search_product.type.ErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PromotionService {

    private final PromotionRepository promotionRepository;
    private final ItemRepository itemRepository;
    private final DtoEntityConverter converter;

    @Transactional
    public void applyPromotion(Long itemId, Long discountRate, LocalDateTime startDate, LocalDateTime endDate) {
        validatePromotionParameters(discountRate, startDate, endDate); // 프로모션 매개변수 유효성 검증

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new CustomError(ITEM_NOT_FOUND));

        // 프로모션 중복 방지
        boolean promotionExists = promotionRepository.existsByItemAndStartDateLessThanEqualAndEndDateGreaterThanEqual(item, endDate, startDate);
        if (promotionExists) {
            log.warn("아이템 ID {}에 대해 중복된 프로모션이 존재합니다.", itemId);
            throw new CustomError(PROMOTION_ALREADY_EXIST);
        }

        Promotion promotion = Promotion.builder()
                .item(item)
                .discountRate(discountRate)
                .startDate(startDate)
                .endDate(endDate)
                .build();

        promotionRepository.save(promotion);
        log.info("아이템 ID {}에 대해 새로운 프로모션이 적용되었습니다: 할인율 {}%, 시작일 {}, 종료일 {}", itemId, discountRate, startDate, endDate);
    }

    private void validatePromotionParameters(Long discountRate, LocalDateTime startDate, LocalDateTime endDate) {
        if (discountRate == null || discountRate.compareTo(0L) < 0) {
            log.error("할인율이 0 이상이어야 합니다. 현재 값: {}", discountRate);
            throw new CustomError(PROMOTION_MUST_OVER_THAN_ZERO);
        }

        if (discountRate.compareTo(100L) > 0) {
            log.error("할인율이 100을 초과할 수 없습니다. 현재 값: {}", discountRate);
            throw new CustomError(PROMOTION_MUST_NOT_OVER_THAN_HUNDRED);
        }

        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            log.error("프로모션 시작 날짜가 종료 날짜보다 이전이어야 합니다. 시작일: {}, 종료일: {}", startDate, endDate);
            throw new CustomError(START_DATE_MUST_BELOW_END_DATE);
        }
    }


    @Transactional(readOnly = true)
    public List<ItemDto> getItemsWithActivePromotions() {
        LocalDateTime now = LocalDateTime.now();
        List<Item> itemsWithPromotions = promotionRepository.findItemsWithActivePromotions(now);
        return itemsWithPromotions.stream()
                .map(converter::convertToItemDto)
                .collect(Collectors.toList());
    }
}
