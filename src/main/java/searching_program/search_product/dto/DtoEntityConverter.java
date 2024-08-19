package searching_program.search_product.dto;

import org.springframework.stereotype.Component;
import searching_program.search_product.domain.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO -> Entity, Entity -> Dto
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
                .lowPrice(itemDto.getLowPrice())
                .maxPrice(itemDto.getMaxPrice())
                .price(itemDto.getPrice())
                .image(itemDto.getImage())
                .itemName(itemDto.getItemName())
                .link(itemDto.getLink())
                .stock(itemDto.getStock())
                .build();
    }

    public ItemFavorite convertToFavoriteEntity(ItemFavoriteDto itemFavoriteDto, Member member, Item item) {
        return ItemFavorite.builder()
                .item(item)
                .member(member)
                .id(itemFavoriteDto.getId())
                .build();
    }

    public Shipment convertToShipmentEntity(ShipmentDto shipmentDto, Order order){
        return Shipment.builder()
                .curLocation(shipmentDto.getCurLocation())
                .estimatedDeliveryDate(shipmentDto.getEstimatedDeliveryDate())
                .shipmentStatus(shipmentDto.getShipmentStatus())
                .id(shipmentDto.getId())
                .order(order)
                .build();
    }

    public Order convertToOrderEntity(OrderDto orderDto, Member member) {
        return Order.builder()
                .orderDate(orderDto.getOrderDate())
                .status(orderDto.getOrderStatus())
                .id(orderDto.getId())
                .member(member)
                .orderItems(new ArrayList<>())
                .build();
    }

    public OrderItem convertToOrderItemEntity(OrderItemDto orderItemDto, Order order, Item item) {
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

    public MemberDto convertToMemberDto(Member member) {
        return MemberDto.builder()
                .id(member.getId())
                .userId(member.getUserId())
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
                .price(item.getPrice())
                .maxPrice(item.getMaxPrice())
                .lowPrice(item.getLowPrice())
                .link(item.getLink())
                .stock(item.getStock())
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

    public OrderDto convertToOrderDto(Order order) {
        List<OrderItemDto> orderItemDtos = order.getOrderItems().stream()
                .map(this::convertToOrderItemDto)
                .collect(Collectors.toList());

        return OrderDto.builder()
                .id(order.getId())
                .orderDate(order.getOrderDate())
                .orderStatus(order.getStatus())
                .memberId(order.getMember().getId())
                .orderItems(orderItemDtos)
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
                .memberId(review.getMember().getId())
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
