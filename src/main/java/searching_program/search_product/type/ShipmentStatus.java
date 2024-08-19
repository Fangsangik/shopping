package searching_program.search_product.type;

public enum ShipmentStatus {
    PENDING("배송 대기 중"),        // 배송 대기 중
    IN_TRANSIT("배송 중"),     // 배송 중
    OUT_FOR_DELIVERY("배달 중"), // 배달 중
    DELIVERED("배달 완료"),      // 배달 완료
    FAILED("배송 실패");          // 배송 실패

    private String message;

    ShipmentStatus(String message) {
        this.message = message;
    }
}
