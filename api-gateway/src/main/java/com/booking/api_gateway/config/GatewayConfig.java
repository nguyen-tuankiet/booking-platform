package com.booking.api_gateway.config;

import com.booking.api_gateway.security.JwtAuthenticationFilter;
import com.booking.api_gateway.security.RateLimitingFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import org.springframework.context.annotation.Primary;
import org.springframework.cloud.gateway.discovery.DiscoveryClientRouteDefinitionLocator;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import reactor.core.publisher.Flux;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class GatewayConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RateLimitingFilter rateLimitingFilter;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()

                // ===== Auth Service =====
                .route("auth-service", r -> r
                        .path("/api/auth/**")
                        .filters(f -> f
                                .stripPrefix(2) // /api/auth/** -> /auth/**
                                .filter(rateLimitingFilter.apply(createRateLimitConfig(200,1)))
                        )
                        .uri("lb://auth-service")
                )

                // ===== Booking Service =====
                .route("booking-service", r -> r
                        .path("/api/bookings/**")
                        .filters(f -> f
                                .stripPrefix(2)
                                .filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config()))
                                .filter(rateLimitingFilter.apply(createRateLimitConfig(100,1)))
                                .retry(retryConfig -> retryConfig
                                        .setRetries(2)
                                        .setMethods(HttpMethod.GET)
                                        .setStatuses(HttpStatus.INTERNAL_SERVER_ERROR, HttpStatus.GATEWAY_TIMEOUT)
                                        .setBackoff(Duration.ofMillis(100), Duration.ofMillis(500), 2, true)
                                )
                        )
                        .uri("lb://booking-service")
                )

                // ===== Payment Service =====
                .route("payment-service", r -> r
                        .path("/api/payments/**")
                        .filters(f -> f
                                .stripPrefix(2)
                                .filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config()))
                                .filter(rateLimitingFilter.apply(createRateLimitConfig(50,1)))
                                .retry(retryConfig -> retryConfig
                                        .setRetries(1)
                                        .setMethods(HttpMethod.GET)
                                        .setStatuses(HttpStatus.INTERNAL_SERVER_ERROR, HttpStatus.GATEWAY_TIMEOUT)
                                        .setBackoff(Duration.ofMillis(200), Duration.ofMillis(800), 2, true)
                                )
                        )
                        .uri("lb://payment-service")
                )

                .build();
    }

    private RateLimitingFilter.Config createRateLimitConfig(int maxRequests, int windowMinutes) {
        RateLimitingFilter.Config config = new RateLimitingFilter.Config();
        config.setMaxRequests(maxRequests);
        config.setWindowMinutes(windowMinutes);
        return config;
    }
}
