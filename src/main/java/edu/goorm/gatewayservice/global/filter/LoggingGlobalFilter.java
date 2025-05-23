package edu.goorm.gatewayservice.global.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.goorm.gatewayservice.global.util.CustomIpUtil;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class LoggingGlobalFilter implements GlobalFilter, Ordered {

  private static final Logger logger = LoggerFactory.getLogger("infoLogger");
  private static final ObjectMapper mapper = new ObjectMapper();

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    String traceId = UUID.randomUUID().toString(); // ✅ traceId 직접 생성
    long start = System.currentTimeMillis();

    return chain.filter(exchange).doFinally(signalType -> {
      long duration = System.currentTimeMillis() - start;

      ServerHttpRequest request = exchange.getRequest();
      ServerHttpResponse response = exchange.getResponse();

      Map<String, Object> logMap = new HashMap<>();
      logMap.put("timestamp", LocalDateTime.now().toString());
      logMap.put("level", "INFO");
      logMap.put("logType", "ROUTE_SUCCESS");
      logMap.put("traceId", traceId); // ✅ 직접 삽입
      logMap.put("service", "gateway-service");
      logMap.put("method", request.getMethod() != null ? request.getMethod().name() : "-");
      logMap.put("url", request.getURI().getPath());
      logMap.put("status", response.getStatusCode() != null ? response.getStatusCode().value() : "-");
      logMap.put("userId", request.getHeaders().getFirst("X-USER-ID"));
      logMap.put("ip", CustomIpUtil.getClientIp(request));
      logMap.put("userAgent", request.getHeaders().getFirst("User-Agent"));
      logMap.put("durationMs", duration);

      try {
        logger.info(mapper.writeValueAsString(logMap));
      } catch (Exception e) {
        logger.error("Failed to log gateway route success", e);
      }
    });
  }

  @Override
  public int getOrder() {
    return -1; // 가장 먼저 실행되도록
  }
}
