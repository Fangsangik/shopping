package searching_program.search_product.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import searching_program.search_product.domain.Orders;
import searching_program.search_product.dto.*;
import searching_program.search_product.service.ItemService;
import searching_program.search_product.service.OrderService;


import java.util.List;

@Slf4j
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final ItemService itemService;
    private final DtoEntityConverter converter;


    /**
     * 아이템 이름으로 주문 조회 메서드
     * http://localhost:8080/orders/search?itemName=mac&page=0&size=4
     */
    @GetMapping("/search")
    public ResponseEntity<Page<OrderDto>> findByItemName(
            @RequestParam String itemName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "4") int size) {
        ItemDto itemDto = new ItemDto();
        itemDto.setItemName(itemName);

        Page<OrderDto> orderDtoPage = orderService.findByItemName(itemDto, page, size);
        return ResponseEntity.ok(orderDtoPage);
    }

    /**
     * 주문 상태 조회 메서드
     * http://localhost:8080/orders/1/status
     */
    @GetMapping("/{orderId}/status")
    public ResponseEntity<OrderDto> findStatus(@PathVariable Long orderId) {
        OrderDto findStatus = orderService.findOrderById(orderId);
        return ResponseEntity.ok(findStatus);
    }

    /**
     * 회원별 모든 주문 조회 메서드
     * http://localhost:8080/orders/members/ik0605?pageNumber=0&size=10
     */
    @GetMapping("/members/{userId}")
    public ResponseEntity<Page<OrderDto>> findOrderMembers(@PathVariable String userId,  // @RequestParam -> @PathVariable로 변경
                                                           @RequestParam(defaultValue = "0") int pageNumber,
                                                           @RequestParam(defaultValue = "10") int size) {
        Page<OrderDto> rst = orderService.findOrdersByMember(userId, pageNumber, size);
        return ResponseEntity.ok(rst);
    }

    /**
     * 주문 생성 메서드
     */

    @PostMapping("/create")
    public ResponseEntity<OrderDto> createOrder(@Valid @RequestBody OrderRequest orderRequest) {
        // OrderRequest에서 OrderDto와 MemberDto 추출
        OrderDto orderDto = orderRequest.getOrderDto();
        MemberDto memberDto = orderRequest.getMemberDto();

        // 서비스 호출하여 주문 생성
        OrderDto createdOrder = orderService.createOrder(orderDto, memberDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
    }


    @PostMapping("/cancel/{orderId}")
    public ResponseEntity<OrderDto> cancelOrder(@PathVariable Long orderId) {
        OrderDto orderDto = orderService.cancelOrder(orderId);
        return ResponseEntity.ok(orderDto);
    }

    @PutMapping("/items/{orderItemId}")
    public ResponseEntity<?> updateOrderItem(
            @PathVariable Long orderItemId,
            @RequestParam int quantity,
            @RequestParam int price) {
        orderService.updateOrder(orderItemId, quantity, price);
        Orders updateOrder = orderService.findOrderEntityById(orderItemId);
        OrderDto orderDto = converter.convertToOrderDto(updateOrder);
        return ResponseEntity.ok(orderDto);
    }

    /**
     * 주문 상태 이력 추적 메서드
     */
    @GetMapping("/{orderId}/history")
    public ResponseEntity<List<OrderStatusHistoryDto>> trackOrder(@PathVariable Long orderId) {
        List<OrderStatusHistoryDto> historyList = orderService.trackOrder(orderId);
        return ResponseEntity.ok(historyList);
    }
}
