package com.booking.api_gateway.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/health")
@RequiredArgsConstructor
@Slf4j
public class HealthController {

    private final DiscoveryClient discoveryClient;
    private final ReactiveRedisTemplate<String, String> redisTemplate;

    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", Instant.now());
        health.put("service", "api-gateway");
        health.put("version", "1.0.0");

        return ResponseEntity.ok(health);
    }

    @GetMapping("/detailed")
    public Mono<ResponseEntity<Map<String, Object>>> detailedHealth() {
        Map<String, Object> health = new HashMap<>();
        health.put("timestamp", Instant.now());
        health.put("service", "api-gateway");

        return checkRedisHealth()
                .zipWith(checkServiceDiscovery())
                .map(tuple -> {
                    health.put("redis", tuple.getT1());
                    health.put("eureka", tuple.getT2());
                    health.put("services", getRegisteredServices());

                    boolean allHealthy = isComponentHealthy(tuple.getT1()) &&
                            isComponentHealthy(tuple.getT2());

                    health.put("status", allHealthy ? "UP" : "DOWN");

                    return ResponseEntity.ok(health);
                })
                .onErrorReturn(ResponseEntity.status(503).body(Map.of(
                        "status", "DOWN",
                        "timestamp", Instant.now(),
                        "error", "Health check failed"
                )));
    }

    private Mono<Map<String, Object>> checkRedisHealth() {
        return redisTemplate.opsForValue()
                .set("health:check", "ping")
                .timeout(Duration.ofSeconds(2))
                .then(redisTemplate.opsForValue().get("health:check"))
                .map(result -> {
                    Map<String, Object> redisHealth = new HashMap<>();
                    redisHealth.put("status", "ping".equals(result) ? "UP" : "DOWN");
                    redisHealth.put("responseTime", "< 2s");
                    return redisHealth;
                })
                .onErrorReturn(Map.of(
                        "status", "DOWN",
                        "error", "Redis connection failed"
                ));
    }

    private Mono<Map<String, Object>> checkServiceDiscovery() {
        return Mono.fromCallable(() -> {
            Map<String, Object> eurekaHealth = new HashMap<>();
            try {
                discoveryClient.getServices();
                eurekaHealth.put("status", "UP");
                eurekaHealth.put("description", "Eureka discovery client is working");
            } catch (Exception e) {
                eurekaHealth.put("status", "DOWN");
                eurekaHealth.put("error", e.getMessage());
            }
            return eurekaHealth;
        });
    }

    private Map<String, Object> getRegisteredServices() {
        Map<String, Object> services = new HashMap<>();
        try {
            discoveryClient.getServices().forEach(serviceName -> {
                int instanceCount = discoveryClient.getInstances(serviceName).size();
                services.put(serviceName, Map.of(
                        "instances", instanceCount,
                        "status", instanceCount > 0 ? "UP" : "DOWN"
                ));
            });
        } catch (Exception e) {
            log.error("Failed to get registered services", e);
            services.put("error", "Failed to retrieve service information");
        }
        return services;
    }

    private boolean isComponentHealthy(Map<String, Object> component) {
        return "UP".equals(component.get("status"));
    }
}