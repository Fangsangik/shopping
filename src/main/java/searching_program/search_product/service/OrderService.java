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
import searching_program.search_product.error.CustomError;
import searching_program.search_product.repository.*;
import searching_program.search_product.type.ErrorCode;
import searching_program.search_product.type.OrderStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static searching_program.search_product.type.ErrorCode.*;
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
    private final PaymentService paymentService;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final BucketRepository bucketRepository;

    /**
     * 주문 생성 메서드
     */
    @Transactional
    public OrderDto createOrder(OrderDto orderDto, MemberDto memberDto) {
        log.info("주문 생성 요청: MemberId = {}, OrderDate = {}", memberDto.getId(), orderDto.getOrderDate());

        // MemberDto의 ID 검증
        if (memberDto.getId() == null) {
            log.error("MemberDto의 ID가 null입니다.");
            throw new CustomError(INVALID_INPUT_VALUE);
        }

        // OrderDto의 주문 항목 검증
        if (orderDto.getOrderItems() == null || orderDto.getOrderItems().isEmpty()) {
            log.error("OrderDto의 주문 항목이 비어있습니다.");
            throw new CustomError(INVALID_INPUT_VALUE);
        }

        Member member = converter.convertToMemberEntity(memberDto);
        Orders orders = converter.convertToOrderEntity(orderDto);
        orders.setMember(member);

        for (OrderItemDto orderItemDto : orderDto.getOrderItems()) {
            Item item = itemRepository.findById(orderItemDto.getItemId())
                    .orElseThrow(() -> new CustomError(ITEM_NOT_FOUND));

            if (item.getStock() < orderItemDto.getQuantity()) {
                throw new CustomError(OUT_OF_STOCK);
            }

            item.setStock(item.getStock() - orderItemDto.getQuantity());
            itemRepository.save(item);

            OrderItem orderItem = converter.convertToOrderItemEntity(orderItemDto, orders, item);
            orders.addOrderItem(orderItem);
        }

        orders.changeStatus(OrderStatus.ORDERED);
        OrderStatusHistory orderStatusHistory = OrderStatusHistory.builder()
                .order(orders)
                .status(OrderStatus.ORDERED)
                .timestamp(LocalDateTime.now())
                .build();
        orders.addStatusHistory(orderStatusHistory.getStatus());

        Orders savedOrder = orderRepository.save(orders);
        log.info("주문 생성 성공: Order ID = {}, Member ID = {}", savedOrder.getId(), memberDto.getId());

        // 결제 서비스 호출
        paymentService.processPayment(member.getId(), savedOrder.getId(), orderDto.getTotalAmount());

        // 장바구니에 있는 아이템 주문 후 제거
        removeOrderedItemsFromBucket(member, orderDto);

        return converter.convertToOrderDto(savedOrder);
    }

    private void removeOrderedItemsFromBucket(Member member, OrderDto orderDto) {
        log.info("장바구니에서 주문된 아이템 제거 시작: Member ID = {}", member.getId());

        for (OrderItemDto orderItemDto : orderDto.getOrderItems()) {
            Long itemId = orderItemDto.getItemId();
            if (itemId == null) {
                log.error("OrderItemDto의 itemId가 null입니다.");
                throw new IllegalArgumentException("Item ID must not be null");
            }

            Item item = itemRepository.findById(itemId)
                    .orElseThrow(() -> new CustomError(ITEM_NOT_FOUND));

            Bucket bucket = bucketRepository.findByMemberAndItem(member, item);
            if (bucket != null) {
                log.info("장바구니에서 아이템 제거: Member ID = {}, Item ID = {}", member.getId(), item.getId());
                bucketRepository.delete(bucket);
            } else {
                log.warn("장바구니에 아이템이 존재하지 않음: Member ID = {}, Item ID = {}", member.getId(), item.getId());
            }
        }
    }

    /**
     * 주문 조회 메서드 (아이템 이름으로 조회)
     */
    @Transactional(readOnly = true)
    public Page<OrderDto> findByItemName(ItemDto itemDto, int page, int size) {
        // PageRequest 객체 생성
        PageRequest pageRequest = PageRequest.of(page, size);

        // itemName으로 주문을 검색하는 로직 구현
        String itemName = itemDto.getItemName();

        // Orders 엔티티를 검색하고 결과를 OrderDto로 변환하여 반환
        Page<Orders> ordersPage = orderRepository.findByItem_ItemNameContaining(itemName, pageRequest);

        return ordersPage.map(converter::convertToOrderDto);
    }

    /**
     * 주문 상태 확인 및 변경 메서드
     */
    @Transactional
    public OrderDto findOrderStatus(OrderDto orderDto) {
        Orders order = orderRepository.findById(orderDto.getId())
                .orElseThrow(() -> new CustomError(ORDER_NOT_FOUND));

        if (order.getStatus() == OrderStatus.ORDERED) {
            return converter.convertToOrderDto(order);
        }

        if (order.getStatus() == OrderStatus.SHIPPED) {
            order.changeStatus(OrderStatus.DELIVERED);
            orderRepository.save(order);
        }

        for (OrderItem orderItem : order.getOrderItems()) {
            Item item = itemRepository.findById(orderItem.getItem().getId())
                    .orElseThrow(() -> new CustomError(ITEM_NOT_FOUND));

            if (item.getStock() < 1) {
                throw new CustomError(OUT_OF_STOCK);
            }

            log.info("Order ID: {}, Item ID: {}, Item Name: {}, Status: {}",
                    order.getId(), item.getId(), item.getItemName(), order.getStatus());
        }

        return converter.convertToOrderDto(order);
    }

    /**
     * 주문 취소 메서드
     */
    @Transactional
    public OrderDto cancelOrder(Long orderId) {
        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomError(ORDER_LIST_NOT_FOUND));

        if (order.getStatus() == OrderStatus.CANCELED) {
            return converter.convertToOrderDto(order);
        }

        // 아이템 재고 복구
        for (OrderItem orderItem : order.getOrderItems()) {
            Item item = orderItem.getItem();
            item.setStock(item.getStock() + orderItem.getQuantity());
            itemRepository.save(item);
        }

        // 주문 상태 변경
        order.changeStatus(OrderStatus.CANCELED);
        return converter.convertToOrderDto(orderRepository.save(order));
    }

    /**
     * 주문 상태 이력 추적 메서드
     */
    @Transactional
    public List<OrderStatusHistoryDto> trackOrder(Long orderId) {
        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

        return order.getStatusHistory()
                .stream()
                .map(converter::convertToOrderStatusHistoryDto)
                .collect(Collectors.toList());
    }

    /**
     * 주문 항목 업데이트 메서드
     */
    @Transactional
    public void updateOrder(Long orderItemId, int quantity, int price) {
        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new CustomError(ORDER_NOT_FOUND));

        if (quantity > orderItem.getItem().getStock()) {
            throw new CustomError(STOCK_EXCEED);
        }

        orderItem.setQuantity(quantity);
        orderItem.setPrice(price);

        orderItemRepository.save(orderItem);
    }

    /**
     * 주문 ID로 주문 조회 메서드
     */
    @Transactional(readOnly = true)
    public OrderDto findOrderById(Long orderId) {
        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomError(ORDER_NOT_FOUND));
        return converter.convertToOrderDto(order);
    }

    /**
     * 회원의 모든 주문 조회 메서드
     */
    @Transactional(readOnly = true)
    public Page<OrderDto> findOrdersByMember(String userId, int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("createdDate").descending());
        Page<Orders> orders = orderRepository.findByMemberUserId(userId, pageable);
        return orders.map(converter::convertToOrderDto);
    }

    @Transactional(readOnly = true)
    public Orders findOrderEntityById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomError(ORDER_NOT_FOUND));
    }

    /**
     * 주문 상태 이력 저장 메서드
     */
    @Transactional
    public void saveOrderStatusHistory(Orders order, OrderStatus status) {
        // 현재 주문의 상태 이력 중 같은 상태가 있는지 확인
        boolean statusExists = order.getStatusHistory().stream()
                .anyMatch(history -> history.getStatus() == status);

        // 상태 이력이 존재하지 않을 경우에만 새로운 이력을 추가
        if (!statusExists) {
            OrderStatusHistory history = OrderStatusHistory.builder()
                    .order(order)  // 주문 객체를 설정
                    .status(status)  // 새로운 상태 설정
                    .timestamp(LocalDateTime.now())  // 현재 시간으로 타임스탬프 설정
                    .build();

            // 주문 객체에 새로운 상태 이력 추가
            order.getStatusHistory().add(history);
            // 새로운 상태 이력을 데이터베이스에 저장
            orderStatusHistoryRepository.save(history);
        }
    }

    // TODO: Email로 배송 완료 알림 보내기 기능 / 결제 처리 연동 확인 프로그램
}

