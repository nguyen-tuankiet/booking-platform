package com.booking.api_gateway.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Component
@Slf4j
public class RateLimitingFilter extends AbstractGatewayFilterFactory<RateLimitingFilter.Config> {

    private final ReactiveRedisTemplate<String, String> redisTemplate;

    public RateLimitingFilter(ReactiveRedisTemplate<String, String> redisTemplate) {
        super(Config.class);
        this.redisTemplate = redisTemplate;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            // Get client identifier (IP address or user ID if authenticated)
            String clientId = getClientId(request);
            String key = "rate_limit:" + clientId + ":" + request.getURI().getPath();

            return redisTemplate.opsForValue()
                    .get(key)
                    .cast(String.class)
                    .map(Integer::parseInt)
                    .defaultIfEmpty(0)
                    .flatMap(currentRequests -> {
                        if (currentRequests >= config.getMaxRequests()) {
                            log.warn("Rate limit exceeded for client: {} on path: {}", clientId, request.getURI().getPath());
                            return onError(exchange, "Rate limit exceeded", HttpStatus.TOO_MANY_REQUESTS);
                        }

                        // Increment counter
                        return redisTemplate.opsForValue()
                                .increment(key)
                                .flatMap(count -> {
                                    if (count == 1) {
                                        // Set expiration for first request
                                        return redisTemplate.expire(key, Duration.ofMinutes(config.getWindowMinutes()))
                                                .then(chain.filter(exchange));
                                    }
                                    return chain.filter(exchange);
                                });
                    });
        };
    }

    private String getClientId(ServerHttpRequest request) {
        // Try to get user ID from header first (if authenticated)
        String userId = request.getHeaders().getFirst("X-User-Id");
        if (userId != null && !userId.isEmpty()) {
            return "user:" + userId;
        }

        // Fall back to IP address
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return "ip:" + xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return "ip:" + xRealIp;
        }

        return "ip:" + request.getRemoteAddress().getAddress().getHostAddress();
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        response.getHeaders().add("X-Rate-Limit-Retry-After-Seconds", "60");

        String errorResponse = String.format(
                "{\"error\": \"%s\", \"message\": \"%s\", \"timestamp\": \"%s\"}",
                httpStatus.getReasonPhrase(),
                message,
                java.time.Instant.now()
        );

        DataBuffer buffer = response.bufferFactory()
                .wrap(errorResponse.getBytes(StandardCharsets.UTF_8));

        return response.writeWith(Mono.just(buffer));
    }

    public static class Config {
        private int maxRequests = 100; // Default: 100 requests per window
        private int windowMinutes = 1; // Default: 1 minute window

        public int getMaxRequests() {
            return maxRequests;
        }

        public void setMaxRequests(int maxRequests) {
            this.maxRequests = maxRequests;
        }

        public int getWindowMinutes() {
            return windowMinutes;
        }

        public void setWindowMinutes(int windowMinutes) {
            this.windowMinutes = windowMinutes;
        }
    }
}