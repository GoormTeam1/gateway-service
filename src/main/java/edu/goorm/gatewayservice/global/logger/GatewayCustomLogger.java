package edu.goorm.gatewayservice.global.logger;

import edu.goorm.gatewayservice.global.util.CustomIpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;

import java.time.LocalDateTime;

public class GatewayCustomLogger {

  private static final Logger logger = LoggerFactory.getLogger(GatewayCustomLogger.class);

  public static void logRequest(
      String logType,
      ServerHttpRequest request,
      String userId,
      String payload
  ) {
    String path = request.getURI().getPath();
    String method = String.valueOf(request.getMethod());
    String ip = CustomIpUtil.getClientIp(request);
    String userAgent = request.getHeaders().getFirst("User-Agent");

    logger.info(String.format(
        "%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s",
        logType,
        LocalDateTime.now(),
        path,
        method,
        userId != null ? userId : "-",
        payload != null ? payload : "-",
        ip != null ? ip : "-",
        userAgent != null ? userAgent : "-"
    ));
  }

  private static String extractClientIp(ServerHttpRequest request) {
    // X-Forwarded-For 헤더 우선
    String forwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
    if (forwardedFor != null) {
      return forwardedFor.split(",")[0].trim();
    }

    // 없으면 remoteAddress 사용
    if (request.getRemoteAddress() != null) {
      return request.getRemoteAddress().getAddress().getHostAddress();
    }

    return "-";
  }
}
