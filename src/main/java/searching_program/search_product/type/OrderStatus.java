package searching_program.search_product.type;

public enum OrderStatus {
    ORDERED("주문 완료"),  // 주문 완료
    SHIPPED("배송 중"),  // 배송 중
    DELIVERED("배송 완료"), // 배송 완료
    CANCELED("주문 취소");  // 주문 취소

    private String message;

    OrderStatus(String message) {
        this.message = message;
    }
}
