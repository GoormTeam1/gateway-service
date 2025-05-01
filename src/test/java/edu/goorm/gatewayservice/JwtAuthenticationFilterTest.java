package edu.goorm.gatewayservice;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

import javax.crypto.SecretKey;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.context.TestPropertySource;

import edu.goorm.gatewayservice.global.exception.BusinessException;
import edu.goorm.gatewayservice.global.exception.ErrorCode;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest
@TestPropertySource(properties = {
    "jwt.secret=veryLongAndSecureSecretKeyForTestingPurposesOnly12345678901234567890"
})
class JwtAuthenticationFilterTest {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private String validToken;
    private String expiredToken;
    private String invalidToken;
    private GatewayFilterChain chain;
    private String secretKey;

    @BeforeEach
    void setUp() {
        secretKey = "veryLongAndSecureSecretKeyForTestingPurposesOnly12345678901234567890";
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));

        // 유효한 토큰 생성
        validToken = Jwts.builder()
                .setSubject("test@example.com")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1시간
                .signWith(key)
                .compact();

        // 만료된 토큰 생성
        expiredToken = Jwts.builder()
                .setSubject("test@example.com")
                .setIssuedAt(new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 2)) // 2시간 전
                .setExpiration(new Date(System.currentTimeMillis() - 1000 * 60 * 60)) // 1시간 전
                .signWith(key)
                .compact();

        // 잘못된 서명의 토큰 생성
        String differentSecretKey = "differentSecretKeyForTestingPurposesOnly1234567890123456789012345";
        SecretKey differentKey = Keys.hmacShaKeyFor(differentSecretKey.getBytes(StandardCharsets.UTF_8));
        invalidToken = Jwts.builder()
                .setSubject("test@example.com")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .signWith(differentKey)
                .compact();

        // Mock GatewayFilterChain
        chain = mock(GatewayFilterChain.class);
    }

    @Test
    void whenValidToken_thenSuccess() {
        AtomicReference<ServerWebExchange> exchangeRef = new AtomicReference<>();

        when(chain.filter(any(ServerWebExchange.class))).thenAnswer(invocation -> {
            ServerWebExchange exchange = invocation.getArgument(0); // ✅ 변경: 더 이상 캐스팅 안 함
            exchangeRef.set(exchange);
            return Mono.empty();
        });

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(jwtAuthenticationFilter.filter(exchange, chain))
                .verifyComplete();

        ServerWebExchange resultExchange = exchangeRef.get();
        String userEmail = resultExchange.getRequest().getHeaders().getFirst("X-User-Email");

        assertEquals("test@example.com", userEmail);
    }

    @Test
    void whenNoToken_thenThrowException() {
        when(chain.filter(any(MockServerWebExchange.class))).thenReturn(Mono.empty());

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(jwtAuthenticationFilter.filter(exchange, chain))
                .expectErrorMatches(throwable -> 
                    throwable instanceof BusinessException && 
                    ((BusinessException) throwable).getErrorCode().equals(ErrorCode.ACCESS_TOKEN_NOT_FOUND))
                .verify();
    }

    @Test
    void whenExpiredToken_thenThrowException() {
        when(chain.filter(any(MockServerWebExchange.class))).thenReturn(Mono.empty());

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + expiredToken)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(jwtAuthenticationFilter.filter(exchange, chain))
                .expectErrorMatches(throwable -> 
                    throwable instanceof BusinessException && 
                    ((BusinessException) throwable).getErrorCode().equals(ErrorCode.EXPIRED_ACCESS_TOKEN))
                .verify();
    }

    @Test
    void whenInvalidToken_thenThrowException() {
        when(chain.filter(any(MockServerWebExchange.class))).thenReturn(Mono.empty());

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + invalidToken)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(jwtAuthenticationFilter.filter(exchange, chain))
                .expectErrorMatches(throwable -> 
                    throwable instanceof BusinessException && 
                    ((BusinessException) throwable).getErrorCode().equals(ErrorCode.TOKEN_SIGNATURE_INVALID))
                .verify();
    }

    @Test
    void whenPublicPath_thenNoTokenRequired() {
        when(chain.filter(any(MockServerWebExchange.class))).thenReturn(Mono.empty());

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/user/login")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(jwtAuthenticationFilter.filter(exchange, chain))
                .verifyComplete();
    }
} 