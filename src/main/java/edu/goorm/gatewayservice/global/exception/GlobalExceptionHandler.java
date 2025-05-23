package edu.goorm.gatewayservice.global.exception;

import edu.goorm.gatewayservice.global.logger.GatewayCustomLogger;
import edu.goorm.gatewayservice.global.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<ApiResponse<Object>> handleBusinessException(BusinessException e, ServerWebExchange exchange) {
        ErrorCode errorCode = e.getErrorCode();
        HttpStatus status = errorCode.getStatus();

        log.warn("비즈니스 예외 발생: {}", errorCode.getMessage());

        GatewayCustomLogger.logError(
            exchange.getRequest(),
            extractUserIdFromHeader(exchange),
            "BUSINESS_ERROR",
            errorCode.getMessage(),
            status.value()
        );

        return ResponseEntity
            .status(status)
            .body(ApiResponse.error(status, errorCode.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ApiResponse<Object>> handleException(Exception e, ServerWebExchange exchange) {
        log.error("예기치 못한 오류 발생", e);

        GatewayCustomLogger.logError(
            exchange.getRequest(),
            extractUserIdFromHeader(exchange),
            "UNEXPECTED_ERROR",
            e.getMessage(),
            HttpStatus.INTERNAL_SERVER_ERROR.value()
        );

        return ResponseEntity
            .status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus())
            .body(ApiResponse.error(
                ErrorCode.INTERNAL_SERVER_ERROR.getStatus(),
                ErrorCode.INTERNAL_SERVER_ERROR.getMessage()
            ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ApiResponse<Object>> handleValidationException(
        MethodArgumentNotValidException e, ServerWebExchange exchange) {

        String message = e.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(DefaultMessageSourceResolvable::getDefaultMessage)
            .findFirst()
            .orElse("잘못된 요청입니다.");

        log.warn("유효성 검사 실패: {}", message);

        GatewayCustomLogger.logError(
            exchange.getRequest(),
            extractUserIdFromHeader(exchange),
            "VALIDATION_ERROR",
            message,
            HttpStatus.BAD_REQUEST.value()
        );

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(HttpStatus.BAD_REQUEST, message));
    }

    private String extractUserIdFromHeader(ServerWebExchange exchange) {
        return exchange.getRequest().getHeaders().getFirst("X-USER-ID");
    }
}
