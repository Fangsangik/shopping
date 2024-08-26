package searching_program.search_product.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import searching_program.search_product.type.ErrorCode;

import javax.persistence.EntityNotFoundException;

/*
Spring MVC 예외 처리 기능 활용, -> 애플리케이션 전반에 발생 할 수 있는 예외 처리
 */

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {


    //IllegalArgumentException
    @ExceptionHandler(IllegalArgumentException.class)
    protected ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException e) {
        ErrorCode errorCode = ErrorCode.INVALID_INPUT_VALUE;
        log.error("handleIllegalArgumentException: {} - {}", errorCode.getMessage(), e.getMessage(), e);
        return buildErrorResponse(errorCode, e.getMessage());
    }

    //EntityNotFoundException
    @ExceptionHandler(EntityNotFoundException.class)
    protected ResponseEntity<Object> handleEntityNotFoundException(EntityNotFoundException ex) {
        ErrorCode errorCode = ErrorCode.ENTITY_NOT_FOUND;
        log.error("handleEntityNotFoundException: {} - {}", errorCode.getMessage(), ex.getMessage(), ex);
        return buildErrorResponse(errorCode, ex.getMessage());
    }

    //특정 예외를 제외한 모든 예외 발생
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<Object> handleAllExceptions(Exception ex) {
        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        log.error("handleAllExceptions: {} - {}", errorCode.getMessage(), ex.getMessage(), ex);
        return buildErrorResponse(errorCode, ex.getMessage());
    }

    //유효성 인자 검사
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        ErrorCode errorCode = ErrorCode.INVALID_INPUT_VALUE;
        String message = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        log.error("handleMethodArgumentNotValid: {} - {}", errorCode.getMessage(), message, ex);
        return buildErrorResponse(errorCode, message);
    }

    private ResponseEntity<Object> buildErrorResponse(ErrorCode errorCode, String message) {
        ApiError apiError = new ApiError(errorCode.getStatus(), errorCode.getMessage(), message);
        return new ResponseEntity<>(apiError, errorCode.getStatus());
    }

    @Getter
    @RequiredArgsConstructor
    public static class ApiError {
        private final HttpStatus status;
        private final String error;
        private final String message;


    }
}
