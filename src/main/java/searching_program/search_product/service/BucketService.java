package searching_program.search_product.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searching_program.search_product.domain.Bucket;
import searching_program.search_product.domain.Item;
import searching_program.search_product.domain.Member;
import searching_program.search_product.dto.BucketDto;
import searching_program.search_product.dto.DtoEntityConverter;
import searching_program.search_product.error.CustomError;
import searching_program.search_product.repository.BucketRepository;
import searching_program.search_product.repository.ItemRepository;
import searching_program.search_product.repository.MemberRepository;
import searching_program.search_product.type.ErrorCode;
import searching_program.search_product.type.ItemStatus;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static searching_program.search_product.type.ErrorCode.*;
import static searching_program.search_product.type.ItemStatus.AVAILABLE;

@Slf4j
@Service
@RequiredArgsConstructor
public class BucketService {

    private final BucketRepository bucketRepository;
    private final DtoEntityConverter converter;
    private final ItemRepository itemRepository;
    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public List<BucketDto> getAllBuckets() {
        List<Bucket> findAllBuckets = bucketRepository.findAll();
        if (findAllBuckets == null || findAllBuckets.isEmpty()) {
            log.warn("장바구니가 비어있습니다.");
            return Collections.emptyList();
        }

        return findAllBuckets.stream()
                .map(converter::convertToBucketDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BucketDto findByItemName(String itemName) {

        if (itemName == null || itemName.trim().isEmpty()) {
            throw new CustomError(INVALID_INPUT_VALUE);
        }

        List<Bucket> buckets = bucketRepository.findByItem_ItemName(itemName);

        if (buckets == null || buckets.isEmpty()) {
            throw new CustomError(ITEM_NOT_FOUND);
        }

        if (buckets.size() > 1) {
            throw new CustomError(NON_UNIQUE_RESULT);
        }

        // 여러 개의 Bucket 중 첫 번째 Bucket을 반환
        Bucket bucket = buckets.get(0);
        return converter.convertToBucketDto(bucket);
    }


    @Transactional
    public BucketDto addItemToBucket(Long memberId, Long itemId, int quantity) {
        if (memberId == null) {
            throw new CustomError(INVALID_INPUT_VALUE);
        }
        if (itemId == null) {
            throw new CustomError(INVALID_INPUT_VALUE);
        }
        if (quantity <= 0) {
            throw new CustomError(ErrorCode.MUST_OVER_THAN_ZERO);
        }

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new CustomError(ITEM_NOT_FOUND));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomError(USER_NOT_FOUND));

        // 기존에 해당 회원의 장바구니에 같은 아이템이 있는지 확인
        Bucket existingBucket = bucketRepository.findByMemberAndItem(member, item);
        if (existingBucket != null) {
            // 기존 장바구니 항목이 있을 경우 수량과 총 가격을 업데이트
            int newQuantity = existingBucket.getQuantity() + quantity;
            existingBucket.setQuantity(newQuantity);
            existingBucket.setItemTotalPrice(item.getItemPrice() * newQuantity); // 총 가격 업데이트
            bucketRepository.save(existingBucket); // 변경 사항 저장
            return converter.convertToBucketDto(existingBucket);
        } else {
            // 장바구니에 아이템이 없다면 새로운 항목 작성
            Bucket bucket = Bucket.builder()
                    .member(member)
                    .item(item)
                    .quantity(quantity)
                    .isSelected(true)
                    .itemTotalPrice(item.getItemPrice() * quantity)
                    .build();

            bucketRepository.save(bucket);
            return converter.convertToBucketDto(bucket);
        }
    }

    @Transactional
    public void validateBucketItems(Long memberId) {
        List<Bucket> buckets = bucketRepository.findByMemberId(memberId);

        for (Bucket bucket : buckets) {
            Item item = bucket.getItem();

            if (item.getStock() < bucket.getQuantity()) {
                log.error("재고 부족: 아이템 ID = {}, 요청 수량 = {}, 현재 재고 = {}", item.getId(), bucket.getQuantity(), item.getStock());
                throw new CustomError(OUT_OF_STOCK);
            }

            if (item.getItemPrice() != bucket.getItemTotalPrice() / bucket.getQuantity()) {
                log.error("가격 변경 감지: 아이템 ID = {}, 기존 가격 = {}, 현재 가격 = {}", item.getId(), bucket.getItemTotalPrice() / bucket.getQuantity(), item.getItemPrice());
                throw new CustomError(ErrorCode.ITEM_PRICE_CHANGED);
            }

            // 3. 상품 판매 가능 여부 확인
            if (item.getItemStatus() != ItemStatus.AVAILABLE) { // AVAILABLE을 ItemStatus.AVAILABLE로 변경
                log.error("상품 판매 불가 상태: 아이템 ID = {}, 현재 상태 = {}", item.getId(), item.getItemStatus());
                throw new CustomError(ErrorCode.ITEM_NOT_SALE);
            }
        }
    }

    @Transactional
    public void removeItemFromBucket(Long bucketId) {
        if (bucketId == null) {
            throw new CustomError(INVALID_INPUT_VALUE);
        }

        Bucket bucket = bucketRepository.findById(bucketId)
                .orElseThrow(() -> new CustomError(BUCKET_NOT_FOUND));
        bucketRepository.delete(bucket);
    }

    @Transactional
    public void clearBucket(Long memberId) {
        if (memberId == null) {
            throw new CustomError(INVALID_INPUT_VALUE);
        }

        List<Bucket> buckets = bucketRepository.findByMemberId(memberId);
        if (buckets == null || buckets.isEmpty()) {
            log.warn("지울 장바구니 항목이 없습니다. 회원 ID: {}", memberId);
            return;
        }

        bucketRepository.deleteAll(buckets);
    }
}
