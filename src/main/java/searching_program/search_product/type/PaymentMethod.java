package searching_program.search_product.type;

public enum PaymentMethod {
    CREDIT("신용카드"),
    ACCOUNT_TRANSFER("통장입금");

    private final String message;

    PaymentMethod(String message) {
        this.message = message;
    }
}
