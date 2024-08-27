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
        log.info("현재 활성 프로모션이 적용된 아이템 수: {}", itemsWithPromotions.size());
        return itemsWithPromotions.stream()
                .map(converter::convertToItemDto)
                .collect(Collectors.toList());
    }

    /**
     * 할인된 가격을 계산하는 메서드
     *
     * @param itemId       아이템 ID
     * @param discountRate 할인율
     * @return 할인된 가격
     */
    public double calculateDiscountedPrice(Long itemId, Long discountRate) {
        // itemId로 Item을 조회
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new CustomError(ITEM_NOT_FOUND));  // 아이템이 없으면 예외 발생

        // 할인율이 null이거나 0 이하인 경우 원래 가격을 반환
        if (discountRate == null || discountRate <= 0) {
            log.info("할인율이 0 이하이므로 원래 가격을 반환합니다: 아이템 ID = {}, 원래 가격 = {}", item.getId(), item.getItemPrice());
            return item.getItemPrice();
        }

        // 할인율이 100을 초과하는 경우 예외 처리
        if (discountRate > 100) {
            log.error("할인율이 100을 초과할 수 없습니다. 현재 할인율: {}", discountRate);
            throw new CustomError(PROMOTION_MUST_NOT_OVER_THAN_HUNDRED);
        }

        // 할인율을 적용하여 할인된 가격 계산
        double discountMultiplier = (100.0 - discountRate) / 100.0;
        double discountedPrice = item.getItemPrice() * discountMultiplier;

        log.info("아이템 ID {}의 할인된 가격 계산: 원래 가격 = {}, 할인율 = {}%, 할인된 가격 = {}",
                item.getId(), item.getItemPrice(), discountRate, discountedPrice);

        return discountedPrice;
    }
}
