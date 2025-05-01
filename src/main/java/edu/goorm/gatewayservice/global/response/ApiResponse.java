package edu.goorm.gatewayservice.global.response;

import edu.goorm.gatewayservice.global.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public class ApiResponse<T> {
	private int status;  // HTTP Status 코드
	private String message;  // 결과 메시지
	private T data;  // 응답 데이터


	// 성공 응답용
	public static <T> ApiResponse<T> success(T data) {
		return new ApiResponse<>(200, "Success", data);
	}
	public static <T> ApiResponse<T> success(HttpStatus httpStatus, String message, T data) {
		return new ApiResponse<>(httpStatus.value(), message, data);
	}
	// 실패 응답용
	public static <T> ApiResponse<T> error(ErrorCode errorCode) {
		return new ApiResponse<>(errorCode.getStatus().value(), errorCode.getMessage(),null);
	}

	public static <T> ApiResponse<T> error(HttpStatus httpStatus, String message) {
		return new ApiResponse<>(httpStatus.value(),message,null);
	}


}
