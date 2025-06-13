//package edu.goorm.gatewayservice;
//
//import java.net.URLEncoder;
//import java.nio.charset.StandardCharsets;
//
//import javax.crypto.SecretKey;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.cloud.gateway.filter.GlobalFilter;
//import org.springframework.core.Ordered;
//import org.springframework.http.server.reactive.ServerHttpRequest;
//import org.springframework.stereotype.Component;
//import org.springframework.web.server.ServerWebExchange;
//
//import edu.goorm.gatewayservice.global.exception.BusinessException;
//import edu.goorm.gatewayservice.global.exception.ErrorCode;
//import io.jsonwebtoken.Claims;
//import io.jsonwebtoken.ExpiredJwtException;
//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.MalformedJwtException;
//import io.jsonwebtoken.UnsupportedJwtException;
//import io.jsonwebtoken.security.Keys;
//import io.jsonwebtoken.security.SignatureException;
//import lombok.RequiredArgsConstructor;
//import reactor.core.publisher.Mono;
//
//@Component
//@RequiredArgsConstructor
//public class JwtAuthenticationFilter implements GlobalFilter, Ordered {
//
//  @Value("${jwt.secret}")
//  private String secretKey;
//
//  @Override
//  public Mono<Void> filter(ServerWebExchange exchange,
//      org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
//    String path = exchange.getRequest().getPath().toString();
//
//    // 로그인, 회원가입은 토큰 없이도 통과
//    if (path.contains("/api/user/login") || path.contains("/api/user/signup") || path.contains("api/auth/reissue")
//        || path.contains("/api/recommendation/search/default")) {
//      return chain.filter(exchange);
//    }
//
//    String token = extractToken(exchange);
//    if (token == null) {
//      if (path.contains("/api/news")) {
//        return chain.filter(exchange);
//      }
//      return Mono.error(new BusinessException(ErrorCode.ACCESS_TOKEN_NOT_FOUND));
//    }
//
//    try {
//      validateToken(token);
//      Claims claims = extractClaims(token);
//      String userEmail = claims.getSubject();
//      String username = (String) claims.get("username");
//      String encodedUsername = URLEncoder.encode(username, StandardCharsets.UTF_8);
//
//      ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
//          .headers(headers -> {
//            headers.remove("X-User-Email");
//            headers.add("X-User-Email", userEmail);
//            headers.remove("X-User-Username");
//            headers.add("X-User-Username", encodedUsername);
//          })
//          .build();
//
//      ServerWebExchange mutatedExchange = exchange.mutate()
//          .request(mutatedRequest)
//          .build();
//
//      return chain.filter(mutatedExchange);
//    } catch (BusinessException e) {
//      return Mono.error(e);
//    }
//  }
//
//  private String extractToken(ServerWebExchange exchange) {
//    String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
//    if (authHeader != null && authHeader.startsWith("Bearer ")) {
//      return authHeader.substring(7);
//    }
//    return null;
//  }
//
//  private void validateToken(String token) {
//    try {
//      SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
//      Jwts.parserBuilder()
//          .setSigningKey(key)
//          .build()
//          .parseClaimsJws(token);
//    } catch (ExpiredJwtException e) {
//      throw new BusinessException(ErrorCode.EXPIRED_ACCESS_TOKEN);
//    } catch (MalformedJwtException e) {
//      throw new BusinessException(ErrorCode.MALFORMED_TOKEN);
//    } catch (SignatureException e) {
//      throw new BusinessException(ErrorCode.TOKEN_SIGNATURE_INVALID);
//    } catch (UnsupportedJwtException e) {
//      throw new BusinessException(ErrorCode.UNSUPPORTED_TOKEN);
//    } catch (IllegalArgumentException e) {
//      throw new BusinessException(ErrorCode.INVALID_ACCESS_TOKEN);
//    }
//  }
//
//  private Claims extractClaims(String token) {
//    SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
//    return Jwts.parserBuilder()
//        .setSigningKey(key)
//        .build()
//        .parseClaimsJws(token)
//        .getBody();
//  }
//
//  @Override
//  public int getOrder() {
//    return -1;
//  }
//}
