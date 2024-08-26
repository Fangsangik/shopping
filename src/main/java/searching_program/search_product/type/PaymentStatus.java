package searching_program.search_product.type;

public enum PaymentStatus {

    PENDING("결제 대기 중"),
    AUTHORIZED("승인"),
    COMPLETED("결제 완료"),
    CANCELED("취소"),
    REFUNDED("환불"),
    FAILED("결제 실패");

    private String message;

    PaymentStatus(String message) {
        this.message = message;
    }
}
