package edu.goorm.gatewayservice;

import edu.goorm.gatewayservice.global.exception.BusinessException;
import edu.goorm.gatewayservice.global.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.http.server.reactive.ServerHttpRequest;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

  @Value("${jwt.secret}")
  private String secretKey;

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
    String path = exchange.getRequest().getPath().toString();

    // 로그인, 회원가입은 토큰 없이도 통과
    if (path.contains("/api/user/login") || path.contains("/api/user/signup") || path.contains("api/auth/reissue")) {
      return chain.filter(exchange);
    }

    String token = extractToken(exchange);
    if (token == null) {
      throw new BusinessException(ErrorCode.ACCESS_TOKEN_NOT_FOUND);
    }

    validateToken(token);

    // JWT 파싱 및 사용자 이메일 추출
    Claims claims = extractClaims(token);

    String userEmail = claims.getSubject(); // 이메일을 subject로 저장했다고 가정

    // 기존 요청을 복제하여 사용자 정보를 강제로 헤더에 추가
    ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
        .header("X-User-Email", userEmail)
        .build();

    ServerWebExchange mutatedExchange = exchange.mutate()
        .request(mutatedRequest)
        .build();

    return chain.filter(mutatedExchange);
  }

  private String extractToken(ServerWebExchange exchange) {
    String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      return authHeader.substring(7);
    }
    return null;
  }

  private void validateToken(String token) {
    try {
      SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
      Jwts.parserBuilder()
          .setSigningKey(key)
          .build()
          .parseClaimsJws(token); // 이 줄에서 모든 예외가 터짐
    } catch (ExpiredJwtException e) {
      throw new BusinessException(ErrorCode.EXPIRED_ACCESS_TOKEN);
    } catch (MalformedJwtException e) {
      throw new BusinessException(ErrorCode.MALFORMED_TOKEN);
    } catch (SignatureException e) {
      throw new BusinessException(ErrorCode.TOKEN_SIGNATURE_INVALID);
    } catch (UnsupportedJwtException e) {
      throw new BusinessException(ErrorCode.UNSUPPORTED_TOKEN);
    } catch (IllegalArgumentException e) {
      throw new BusinessException(ErrorCode.INVALID_ACCESS_TOKEN);
    }
  }


  private Claims extractClaims(String token) {
    SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    return Jwts.parserBuilder()
        .setSigningKey(key)
        .build()
        .parseClaimsJws(token)
        .getBody();
  }

  @Override
  public int getOrder() {
    return -1;
  }
}
