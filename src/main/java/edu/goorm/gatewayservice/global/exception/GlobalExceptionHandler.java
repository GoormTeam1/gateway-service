package edu.goorm.gatewayservice.global.exception;


import edu.goorm.gatewayservice.global.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	// 비즈니스 예외 처리
	@ExceptionHandler(BusinessException.class)
	protected ResponseEntity<ApiResponse<Object>> handleBusinessException(BusinessException e) {
		ErrorCode errorCode = e.getErrorCode();
		return ResponseEntity
				.status(errorCode.getStatus())
				.body(ApiResponse.error(errorCode.getStatus(), errorCode.getMessage()));
	}

	// 예상 못한 모든 예외 처리
	@ExceptionHandler(Exception.class)
	protected ResponseEntity<ApiResponse<Object>> handleException(Exception e) {
		return ResponseEntity
				.status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus())
				.body(ApiResponse.error(
						ErrorCode.INTERNAL_SERVER_ERROR.getStatus(),
						ErrorCode.INTERNAL_SERVER_ERROR.getMessage()
				));
	}
}
