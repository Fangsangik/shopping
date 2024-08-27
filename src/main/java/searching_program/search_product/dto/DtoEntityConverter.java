package searching_program.search_product.dto;

import org.springframework.stereotype.Component;
import searching_program.search_product.domain.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * TODO -> MapStruct 사용 (자동 변환)
 *
 */
@Component
public class DtoEntityConverter {

    public Member convertToMemberEntity(MemberDto memberDto) {
        return Member.builder()
                .id(memberDto.getId())
                .userId(memberDto.getUserId())
                .username(memberDto.getUsername())
                .age(memberDto.getAge())
                .address(memberDto.getAddress())
                .grade(memberDto.getGrade())
                .paymentMethod(memberDto.getPaymentMethod())
                .memberStatus(memberDto.getMemberStatus())
                .password(memberDto.getPassword())
                .build();
    }

    public Item convertToItemEntity(ItemDto itemDto) {
        return Item.builder()
                .id(itemDto.getId())
                .itemPrice(itemDto.getItemPrice())
                .lowPrice(itemDto.getLowPrice())
                .maxPrice(itemDto.getMaxPrice())
                .image(itemDto.getImage())
                .itemName(itemDto.getItemName())
                .link(itemDto.getLink())
                .stock(itemDto.getStock())
                .category(convertToCategoryEntity(itemDto.getCategoryDto()))
                .build();
    }

    public ItemFavorite convertToFavoriteEntity(ItemFavoriteDto itemFavoriteDto, Member member, Item item) {
        return ItemFavorite.builder()
                .id(itemFavoriteDto.getId())
                .item(item)
                .member(member)
                .id(itemFavoriteDto.getId())
                .build();
    }

    public Shipment convertToShipmentEntity(ShipmentDto shipmentDto, Orders order) {
        return Shipment.builder()
                .curLocation(shipmentDto.getCurLocation())
                .estimatedDeliveryDate(shipmentDto.getEstimatedDeliveryDate())
                .shipmentStatus(shipmentDto.getShipmentStatus())
                .id(shipmentDto.getId())
                .order(order)
                .build();
    }

    public Orders convertToOrderEntity(OrderDto orderDto) {
        return Orders.builder()
                .orderDate(orderDto.getOrderDate())
                .status(orderDto.getStatus())
                .id(orderDto.getId())
                .totalAmount(orderDto.getTotalAmount())
                .statusHistory(new ArrayList<>())
                .createdDate(orderDto.getCreatedDate())
                .orderItems(new ArrayList<>())
                .build();
    }

    public OrderItem convertToOrderItemEntity(OrderItemDto orderItemDto, Orders order, Item item) {
        return OrderItem.builder()
                .quantity(orderItemDto.getQuantity())
                .order(order)
                .price(orderItemDto.getPrice())
                .id(orderItemDto.getId())
                .item(item)
                .build();
    }

    public Promotion convertToPromotionEntity(PromotionDto promotionDto, Item item) {
        return Promotion.builder()
                .startDate(promotionDto.getStartDate())
                .endDate(promotionDto.getEndDate())
                .discountRate(promotionDto.getDiscountRate())
                .item(item)
                .build();
    }

    public Review convertToReviewEntity(ReviewDto reviewDto, Item item, Member member) {
        return Review.builder()
                .rate(reviewDto.getRate())
                .reviewText(reviewDto.getReviewText())
                .item(item)
                .member(member)
                .id(reviewDto.getId())
                .build();
    }

    public Category convertToCategoryEntity(CategoryDto categoryDto) {
        return Category.builder()
                .id(categoryDto.getId())
                .name(categoryDto.getName())
                .build();
    }

    public CategoryDto convertToCategoryDto(Category category) {
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }


    public MemberDto convertToMemberDto(Member member) {
        return MemberDto.builder()
                .id(member.getId())
                .userId(member.getUserId())
                .password(member.getPassword())
                .username(member.getUsername())
                .age(member.getAge())
                .address(member.getAddress())
                .grade(member.getGrade())
                .paymentMethod(member.getPaymentMethod())
                .memberStatus(member.getMemberStatus())
                .build();
    }

    public ItemDto convertToItemDto(Item item) {
        return ItemDto.builder()
                .id(item.getId())
                .image(item.getImage())
                .itemName(item.getItemName())
                .itemPrice(item.getItemPrice())
                .maxPrice(item.getMaxPrice())
                .lowPrice(item.getLowPrice())
                .link(item.getLink())
                .stock(item.getStock())
                .categoryDto(convertToCategoryDto(item.getCategory()))
                .build();
    }

    public ItemFavoriteDto convertToFavoriteDto(ItemFavorite itemFavorite) {
        return ItemFavoriteDto.builder()
                .id(itemFavorite.getId())
                .memberId(itemFavorite.getMember().getId())
                .itemId(itemFavorite.getItem().getId())
                .build();
    }

    public OrderStatusHistoryDto convertToOrderStatusHistoryDto(OrderStatusHistory history) {
        return OrderStatusHistoryDto.builder()
                .status(history.getStatus())
                .timestamp(history.getTimestamp())
                .build();
    }

    public OrderDto convertToOrderDto(Orders order) {
        List<OrderItemDto> orderItemDtos = order.getOrderItems().stream()
                .map(this::convertToOrderItemDto)
                .collect(Collectors.toList());

        List<OrderStatusHistoryDto> statusHistoryDtos = order.getStatusHistory().stream()
                .map(this::convertToOrderStatusHistoryDto)
                .collect(Collectors.toList());

        return OrderDto.builder()
                .id(order.getId())
                .totalAmount(order.getTotalAmount())
                .orderDate(order.getOrderDate())
                .orderStatusHistories(statusHistoryDtos)
                .status(order.getStatus())
                .createdDate(order.getCreatedDate())
                .userId(order.getMember().getUserId())
                .orderItems(orderItemDtos)
                .build();
    }

    public BucketDto convertToBucketDto(Bucket bucket) {
        return BucketDto.builder()
                .id(bucket.getId())
                .memberId(bucket.getMember().getId())
                .itemName(bucket.getItem().getItemName())
                .itemTotalPrice(bucket.calculateItemTotalPrice())
                .addedAt(bucket.getAddedAt())
                .quantity(bucket.getQuantity())
                .build();
    }

    public PaymentDto convertToPaymentDto(Payment payment) {
        return PaymentDto.builder()
                .id(payment.getId())
                .orderId(payment.getOrder().getId())
                .amount(payment.getAmount())
                .status(payment.getPaymentStatus())
                .paymentDate(payment.getPaymentDate())
                .build();
    }

    public OrderItemDto convertToOrderItemDto(OrderItem orderItem) {
        return OrderItemDto.builder()
                .id(orderItem.getId())
                .quantity(orderItem.getQuantity())
                .price(orderItem.getPrice())
                .itemId(orderItem.getItem().getId())
                .build();
    }

    public PromotionDto convertToPromotionDto(Promotion promotion) {
        return PromotionDto.builder()
                .id(promotion.getId())
                .startDate(promotion.getStartDate())
                .endDate(promotion.getEndDate())
                .discountRate(promotion.getDiscountRate())
                .itemId(promotion.getItem().getId())
                .build();
    }

    public ReviewDto convertToReviewDto(Review review) {
        return ReviewDto.builder()
                .id(review.getId())
                .rate(review.getRate())
                .reviewText(review.getReviewText())
                .itemId(review.getItem().getId())
                .userId(review.getMember().getUserId())
                .build();
    }

    public ShipmentDto convertToShipmentDto(Shipment shipment) {
        return ShipmentDto.builder()
                .shipmentStatus(shipment.getShipmentStatus())
                .estimatedDeliveryDate(shipment.getEstimatedDeliveryDate())
                .id(shipment.getId())
                .curLocation(shipment.getCurLocation())
                .build();
    }
}
