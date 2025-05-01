package edu.goorm.gatewayservice.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

  // 400
  INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "잘못된 입력입니다."),
  // 401
  UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
  INVALID_PASSWORD(HttpStatus.UNAUTHORIZED,"비밀번호가 유효하지 않습니다."),
  INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 Access Token토큰입니다."),
  INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED,"유효하지 않은 Refresh Token토큰입니다."),
  EXPIRED_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "Access Token이 만료되었습니다."),
  EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "Refresh Token이 만료되었습니다."),
  NO_AUTH_HEADER(HttpStatus.UNAUTHORIZED, "Authorization 헤더가 존재하지 않습니다."),
  MALFORMED_TOKEN(HttpStatus.UNAUTHORIZED, "토큰 형식이 잘못되었습니다."),
  UNSUPPORTED_TOKEN(HttpStatus.UNAUTHORIZED, "지원하지 않는 토큰입니다."),
  TOKEN_SIGNATURE_INVALID(HttpStatus.UNAUTHORIZED, "토큰 서명이 유효하지 않습니다."),
  // 403
  FORBIDDEN(HttpStatus.FORBIDDEN, "권한이 없습니다."),
  // 404
  ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 자원을 찾을 수 없습니다."),
  USER_NOT_FOUND(HttpStatus.NOT_FOUND,"존재하지 않는 유저입니다."),
  ACCESS_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND,"토큰이 존재하지 않습니다."),
  // 409
  DUPLICATE_RESOURCE(HttpStatus.CONFLICT, "이미 존재하는 자원입니다."),
  // 500
  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");

  private final HttpStatus status;
  private final String message;

  ErrorCode(HttpStatus status, String message) {
    this.status = status;
    this.message = message;
  }
}
