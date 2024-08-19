package searching_program.search_product.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.weaver.ast.Or;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searching_program.search_product.domain.*;
import searching_program.search_product.dto.*;
import searching_program.search_product.repository.*;
import searching_program.search_product.type.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static searching_program.search_product.type.OrderStatus.CANCELED;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;
    private final OrderItemRepository orderItemRepository;
    private final DtoEntityConverter converter;
    private final ShipmentRepository shipmentRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;

    @Transactional(readOnly = true)
        //주문한 물품 이름으로 검색
    Page<OrderDto> findByItemName(ItemDto itemDto, int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber, 5, Sort.by("itemName").ascending());
        Page<Order> orders = orderRepository.findByItemName(itemDto.getItemName(), pageable);
        return orders.map(converter::convertToOrderDto);
    }

    @Transactional(readOnly = true)
    public OrderDto findOrderStatus(OrderDto orderDto) {
        Order order = orderRepository.findById(orderDto.getId())
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

        // 주문 상태가 ORDERED인 경우, 상태를 변경하지 않고 그대로 반환
        if (order.getStatus() == OrderStatus.ORDERED) {
            return converter.convertToOrderDto(order);
        }

        // 주문 상태가 SHIPPED인 경우, 상태를 DELIVERED로 변경
        if (order.getStatus() == OrderStatus.SHIPPED) {
            order.setStatus(OrderStatus.DELIVERED);
            orderRepository.save(order);
        }

        for (OrderItem orderItem : order.getOrderItems()) {
            Item item = itemRepository.findById(orderItem.getItem().getId())
                    .orElseThrow(() -> new IllegalArgumentException("아이탬을 찾을 수 없습니다."));


            if (item.getStock() < 1) {
                throw new IllegalArgumentException("아이탬 재고가 부족합니다. 아이탬 이름 : " + item.getItemName());
            }

            // 로그 기록 로직 추가
            log.info("Order ID: {}, Item ID: {}, Item Name: {}, Status: {}",
                    order.getId(), item.getId(), item.getItemName(), order.getStatus());
        }

        return converter.convertToOrderDto(order);
    }

    @Transactional
    public OrderDto cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문 내역을 찾을 수 없습니다."));

        if (order.getStatus() == CANCELED) {
           return converter.convertToOrderDto(order);
        }

        for (OrderItem orderItem : order.getOrderItems()) {
            Item item = orderItem.getItem();
            item.setStock(item.getStock() + orderItem.getQuantity());
            itemRepository.save(item);
        }

        order.setStatus(CANCELED);
        return converter.convertToOrderDto(orderRepository.save(order));
    }

    @Transactional
    public List<OrderStatusHistoryDto> trackOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

        Shipment shipment = shipmentRepository.findByOrder(order)
                .orElseThrow(() -> new IllegalArgumentException("배송 정보를 찾을 수 없습니다."));
        return order.getStatusHistory()
                .stream()
                .map(converter::convertToOrderStatusHistoryDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateOrder(Long orderItemId, int quantity, int price) {
        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new IllegalArgumentException("주문 항목을 찾을 수 없습니다."));

        if (quantity > orderItem.getItem().getStock()) {
            throw new IllegalArgumentException("주문 수량이 재고를 초과했습니다.");
        }

        orderItem.setQuantity(quantity);
        orderItem.setPrice(price);

        orderItemRepository.save(orderItem);
    }

    @Transactional(readOnly = true)
    public Page<OrderDto> findOrdersByMember(Long memberId, int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("createdDate").descending());
        Page<Order> orders = orderRepository.findByMemberId(memberId, pageable);
        return orders.map(converter::convertToOrderDto);
    }

    @Transactional
    public void saveOrderStatusHistory (Order order, OrderStatus status) {
        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(order)
                .status(status)
                .timestamp(LocalDateTime.now())
                .build();

        orderStatusHistoryRepository.save(history);
    }

    /**
     * TODO : Email로 배송 완료 알림 보내기 기능 / 결제 처리 연동 확인 프로그램
     */
}
