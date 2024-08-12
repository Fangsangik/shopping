package searching_program.search_product.type;

import lombok.Getter;

@Getter
public enum MemberStatus {

    ACTIVE("활동"),
    UN_ACTIVE("미활동");

    private final String message;

    MemberStatus(String message) {
        this.message = message;
    }
}
