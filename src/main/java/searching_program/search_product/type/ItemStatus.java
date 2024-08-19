package searching_program.search_product.type;

public enum ItemStatus {

    AVAILABLE("판매중"),   // 판매 중
    OUT_OF_STOCK("품절"), // 품절
    DISCONTINUED("단종");  // 단종


    private final String message;

    ItemStatus(String message) {
        this.message = message;
    }

}
