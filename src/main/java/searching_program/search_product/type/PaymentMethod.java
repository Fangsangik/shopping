package searching_program.search_product.type;

public enum PaymentMethod {
    CREDIT_CARD("CREDIT_CARD"),
    ACCOUNT_TRANSFER("ACCOUNT_TRANSFER");

    private final String message;

    PaymentMethod(String message) {
        this.message = message;
    }
}