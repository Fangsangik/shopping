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

        if (discountRate.compareTo(0L) < 0) {
            throw new CustomError(PROMOTION_MUST_OVER_THAN_ZERO);
        }

        if (discountRate.compareTo(100L) > 0) {
            throw new CustomError(PROMOTION_MUST_NOT_OVER_THAN_HUNDRED);
        }

        if (startDate.isAfter(endDate)) {
            throw new CustomError(START_DATE_MUST_BELOW_END_DATE);
        }

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new CustomError(ITEM_NOT_FOUND));

        //프로모션 중복 방지
        boolean promotionExists = promotionRepository.existsByItemAndStartDateLessThanEqualAndEndDateGreaterThanEqual(item, endDate, startDate);
        if (promotionExists) {
            throw new CustomError(PROMOTION_ALREADY_EXIST);
        }

        Promotion promotion = Promotion.builder()
                .item(item)
                .discountRate(discountRate)
                .startDate(startDate)
                .endDate(endDate)
                .build();

        promotionRepository.save(promotion);
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
