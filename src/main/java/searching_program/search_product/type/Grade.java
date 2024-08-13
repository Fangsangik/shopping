package searching_program.search_product.type;

public enum Grade {
    VIP("VIP"),
    PLATINUM("PLATINUM"),
    NORMAL("NORMAL"),
    BASIC("BASIC"); // BASIC 추가

    private final String message;

    Grade(String message) {
        this.message = message;
    }
}