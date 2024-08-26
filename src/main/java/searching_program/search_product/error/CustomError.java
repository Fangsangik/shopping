package searching_program.search_product.error;

import lombok.Getter;
import searching_program.search_product.type.ErrorCode;

@Getter
public class CustomError extends RuntimeException{
    private final ErrorCode errorCode;


    public CustomError(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
