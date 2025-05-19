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

        log.warn("비즈니스 예외 발생: {}", errorCode.getMessage());

        GatewayCustomLogger.logRequest(
            "BUSINESS_ERROR",
            exchange.getRequest(),
            extractUserIdFromHeader(exchange), // 헤더에 X-USER-ID 같은 값이 있다면
            String.format("{\"errorMessage\": \"%s\"}", errorCode.getMessage())
        );

        return ResponseEntity
            .status(errorCode.getStatus())
            .body(ApiResponse.error(errorCode.getStatus(), errorCode.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ApiResponse<Object>> handleException(Exception e, ServerWebExchange exchange) {
        log.error("예기치 못한 오류 발생", e);

        GatewayCustomLogger.logRequest(
            "UNEXPECTED_ERROR",
            exchange.getRequest(),
            extractUserIdFromHeader(exchange),
            String.format("{\"errorMessage\": \"%s\"}", e.getMessage())
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

        GatewayCustomLogger.logRequest(
            "VALIDATION_ERROR",
            exchange.getRequest(),
            extractUserIdFromHeader(exchange),
            String.format("{\"validationError\": \"%s\"}", message)
        );

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(HttpStatus.BAD_REQUEST, message));
    }

    // ✅ 유저 ID를 헤더에서 추출 (JWT 기반이면 여기에 토큰 파싱도 가능)
    private String extractUserIdFromHeader(ServerWebExchange exchange) {
        return exchange.getRequest().getHeaders().getFirst("X-USER-ID");
    }
}
