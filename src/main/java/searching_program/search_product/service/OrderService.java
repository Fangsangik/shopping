package searching_program.search_product.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static searching_program.search_product.type.OrderStatus.*;
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

    @Transactional
    public OrderDto createOrder(OrderDto orderDto, MemberDto memberDto) {
        log.info("주문 생성 요청 : MemberId = {}, OrderDate ={}", memberDto.getId(), orderDto.getOrderDate());

        Orders orders = converter.convertToOrderEntity(orderDto, converter.convertToMemberEntity(memberDto));

        for (OrderItemDto orderItemDto : orderDto.getOrderItems()) {
            Item item = itemRepository.findById(orderItemDto.getItemId())
                    .orElseThrow(() -> new IllegalArgumentException("아이템을 찾을 수 없습니다."));

            if (item.getStock() < orderItemDto.getQuantity()) {
                throw new IllegalArgumentException("아이템 재고가 부족합니다. " + item.getItemName());
            }

            item.setStock(item.getStock() - orderItemDto.getQuantity());
            itemRepository.save(item);

            OrderItem orderItem = converter.convertToOrderItemEntity(orderItemDto, orders, item);
            orders.addOrderItem(orderItem);
        }

        orders.setStatus(OrderStatus.ORDERED);

        // 상태 기록을 생성하고 추가합니다.
        OrderStatusHistory orderStatusHistory = OrderStatusHistory.builder()
                .order(orders)
                .status(OrderStatus.ORDERED)
                .timestamp(LocalDateTime.now())
                .build();
        orders.getStatusHistory().add(orderStatusHistory);

        Orders savedOrder = orderRepository.save(orders);
        saveOrderStatusHistory(savedOrder, OrderStatus.ORDERED);
        log.info("주문 생성 성공: Order ID={}, Member ID={}", savedOrder.getId(), memberDto.getId());
        return converter.convertToOrderDto(savedOrder);
    }

    @Transactional(readOnly = true)
    public Page<OrderDto> findByItemName(ItemDto itemDto, int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber, 5, Sort.by("itemName").ascending());
        Page<Orders> orders = orderRepository.findByItemItemName(itemDto.getItemName(), pageable);
        return orders.map(converter::convertToOrderDto);
    }

    @Transactional(readOnly = true)
    public OrderDto findOrderStatus(OrderDto orderDto) {
        Orders order = orderRepository.findById(orderDto.getId())
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

        if (order.getStatus() == OrderStatus.ORDERED) {
            return converter.convertToOrderDto(order);
        }

        if (order.getStatus() == OrderStatus.SHIPPED) {
            order.setStatus(OrderStatus.DELIVERED);
            orderRepository.save(order);
        }

        for (OrderItem orderItem : order.getOrderItems()) {
            Item item = itemRepository.findById(orderItem.getItem().getId())
                    .orElseThrow(() -> new IllegalArgumentException("아이템을 찾을 수 없습니다."));

            if (item.getStock() < 1) {
                throw new IllegalArgumentException("아이템 재고가 부족합니다. 아이템 이름: " + item.getItemName());
            }

            log.info("Order ID: {}, Item ID: {}, Item Name: {}, Status: {}",
                    order.getId(), item.getId(), item.getItemName(), order.getStatus());
        }

        return converter.convertToOrderDto(order);
    }

    @Transactional
    public OrderDto cancelOrder(Long orderId) {
        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문 내역을 찾을 수 없습니다."));

        if (order.getStatus() == OrderStatus.CANCELED) {
            return converter.convertToOrderDto(order);
        }

        for (OrderItem orderItem : order.getOrderItems()) {
            Item item = orderItem.getItem();
            item.setStock(item.getStock() + orderItem.getQuantity());
            itemRepository.save(item);
        }

        order.setStatus(OrderStatus.CANCELED);
        return converter.convertToOrderDto(orderRepository.save(order));
    }

    @Transactional
    public List<OrderStatusHistoryDto> trackOrder(Long orderId) {
        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

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
    public OrderDto findOrderById(Long orderId) {
        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID로 주문을 찾을 수 없습니다."));
        return converter.convertToOrderDto(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderDto> findOrdersByMember(String userId, int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("createdDate").descending());
        Page<Orders> orders = orderRepository.findByMemberUserId(userId, pageable);
        return orders.map(converter::convertToOrderDto);
    }

    @Transactional
    public void saveOrderStatusHistory(Orders order, OrderStatus status) {
        boolean statusExists = order.getStatusHistory().stream()
                .anyMatch(history -> history.getStatus() == status);

        if (!statusExists) {
            OrderStatusHistory history = OrderStatusHistory.builder()
                    .order(order)
                    .status(status)
                    .timestamp(LocalDateTime.now())
                    .build();

            order.getStatusHistory().add(history);
            orderStatusHistoryRepository.save(history);
        }
    }
    // TODO: Email로 배송 완료 알림 보내기 기능 / 결제 처리 연동 확인 프로그램
}
