package searching_program.search_product.type;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "잘못된 입력값 입니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "허용되지 않은 메서드 입니다"),
    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "엔티티를 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 에러"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    USER_DUPLICATE(HttpStatus.CONFLICT, "사용자가 존재 합니다."),
    PASSWORD_NOT_MATCH(HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다."),
    AGE_MUST_GREATER_THAN_ZERO(HttpStatus.BAD_REQUEST, "나이는 0보다 커야 합니다."),
    PAYMENT_FAILED(HttpStatus.BAD_REQUEST, "결제 처리에 실패했습니다."),
    PAYMENT_CANCELED(HttpStatus.BAD_REQUEST, "이미 취소된 결제 입니다."),
    PAYMENT_REFUND(HttpStatus.BAD_REQUEST, "완료된 결제만 환불할 수 있습니다."),
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 거래를 찾을 수 없습니다"),
    BUCKET_NOT_FOUND(HttpStatus.NOT_FOUND, "장바구니를 찾을 수 없습니다."),
    PAYMENT_NOT_AVAILABLE(HttpStatus.BAD_REQUEST, "유효하지 않은 금액입니다."),
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다."),
    ORDER_ALREADY_COMPLETED(HttpStatus.BAD_REQUEST, "이미 완료된 주문입니다."),
    ORDER_LIST_NOT_FOUND(HttpStatus.NOT_FOUND, "주문 내역을 찾을 수 없습니다."),
    ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "아이탬을 찾을 수 없습니다"),
    OUT_OF_STOCK(HttpStatus.BAD_REQUEST, "아이탬 제고 없음"),
    STOCK_EXCEED(HttpStatus.BAD_REQUEST, "주문 수량을 초과했습니다."),
    MUST_OVER_THAN_ZERO(HttpStatus.BAD_REQUEST, "수량이 0은 넘어야 합니다."),
    ITEM_PRICE_CHANGED(HttpStatus.BAD_REQUEST, "아이탬 가격이 변동 되었습니다."),
    ITEM_NOT_SALE(HttpStatus.BAD_REQUEST, "더이상 판매되지 않는 아이탬 입니다."),
    PROMOTION_MUST_OVER_THAN_ZERO(HttpStatus.BAD_REQUEST, "할인율은 0보다 커야 합니다."),
    PROMOTION_MUST_NOT_OVER_THAN_HUNDRED(HttpStatus.BAD_REQUEST, "할인율은 100을 초과 할 수 없습니다"),
    START_DATE_MUST_BELOW_END_DATE(HttpStatus.BAD_REQUEST, "시작 날짜가 종료 날짜보다 이후일 수 없습니다."),
    PROMOTION_ALREADY_EXIST(HttpStatus.CONFLICT, "해당 프로모션이 존재 합니다"),
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 리뷰를 찾을 수 없습니다."),
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 카테고리를 찾을 수 없습니다."),
    NON_UNIQUE_RESULT(HttpStatus.BAD_REQUEST, "해당 결과 값을 찾을 수 없습니다."),
    PAYMENT_CANNOT_BE_CANCELED(HttpStatus.BAD_REQUEST, "결제가 취소 될 수 없습니다."),
    PAYMENT_REFUND_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "환불 불가 합니다."),
    ACCOUNT_IS_LOCKED(HttpStatus.BAD_REQUEST, "계정이 잠겼습니다"),
    ITEM_DUPLICATE(HttpStatus.CONFLICT, "아이탬이 중복됩니다."),
    NO_ACTIVE_PROMOTION(HttpStatus.NOT_FOUND, "활성화된 쿠폰이 아닙니다."),
    INVALID_COUPON_CODE(HttpStatus.BAD_REQUEST, "사용이 불가능한 쿠폰입니다");


    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}