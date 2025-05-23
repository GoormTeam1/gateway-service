package edu.goorm.gatewayservice.global.logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.goorm.gatewayservice.global.util.CustomIpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.server.reactive.ServerHttpRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class GatewayCustomLogger {

  private static final Logger infoLogger = LoggerFactory.getLogger("infoLogger");
  private static final Logger errorLogger = LoggerFactory.getLogger("errorLogger");
  private static final ObjectMapper mapper = new ObjectMapper();

  public static void logRequest(
      String logType,
      ServerHttpRequest request,
      String userId,
      String payload
  ) {
    try {
      Map<String, Object> logMap = new HashMap<>();
      logMap.put("timestamp", LocalDateTime.now().toString());
      logMap.put("level", "INFO");
      logMap.put("logType", logType);
      logMap.put("traceId", MDC.get("traceId"));
      logMap.put("service", "gateway-service");
      request.getMethod();
      logMap.put("method", request.getMethod().name());
      logMap.put("url", request.getURI().getPath());
      logMap.put("userId", userId != null ? userId : "-");
      logMap.put("payload", payload != null ? payload : "-");
      logMap.put("ip", CustomIpUtil.getClientIp(request));
      logMap.put("userAgent", request.getHeaders().getFirst("User-Agent"));

      infoLogger.info(mapper.writeValueAsString(logMap));
    } catch (Exception e) {
      errorLogger.error("Failed to log gateway request", e);
    }
  }

  public static void logError(
      ServerHttpRequest request,
      String userId,
      String logType,
      String errorMessage,
      int status
  ) {
    try {
      Map<String, Object> logMap = new HashMap<>();
      logMap.put("timestamp", LocalDateTime.now().toString());
      logMap.put("level", "ERROR");
      logMap.put("logType", logType);
      logMap.put("traceId", MDC.get("traceId"));
      logMap.put("service", "gateway-service");
      logMap.put("method", request.getMethod() != null ? request.getMethod().name() : "-");
      logMap.put("url", request.getURI().getPath());
      logMap.put("status", status);
      logMap.put("userId", userId != null ? userId : "-");
      logMap.put("ip", CustomIpUtil.getClientIp(request));
      logMap.put("userAgent", request.getHeaders().getFirst("User-Agent"));

      Map<String, Object> error = new HashMap<>();
      error.put("message", errorMessage);
      logMap.put("error", error);

      errorLogger.error(mapper.writeValueAsString(logMap));
    } catch (Exception ex) {
      errorLogger.error("Failed to log gateway error", ex);
    }
  }

}
