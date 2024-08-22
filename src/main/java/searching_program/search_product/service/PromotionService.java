package searching_program.search_product.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searching_program.search_product.domain.Item;
import searching_program.search_product.domain.Promotion;
import searching_program.search_product.dto.DtoEntityConverter;
import searching_program.search_product.dto.ItemDto;
import searching_program.search_product.repository.ItemRepository;
import searching_program.search_product.repository.PromotionRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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
            throw new IllegalArgumentException("할인율은 0보다 작을 수 없습니다.");
        }

        if (discountRate.compareTo(100L) > 0) {
            throw new IllegalArgumentException("할인율은 100% 이상일 수 없습니다.");
        }

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("시작 날짜가 종료 날짜보다 이후일 수 없습니다.");
        }

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("아이탬을 찾을 수 없습니다."));

        //프로모션 중복 방지
        boolean promotionExists = promotionRepository.existsByItemAndStartDateLessThanEqualAndEndDateGreaterThanEqual(item, endDate, startDate);
        if (promotionExists) {
            throw new IllegalStateException("해당 기간 동안 이미 프로모션이 적용되어 있습니다.");
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
