package com.booking.booking_service.controller;

import com.booking.common_library.dto.ApiResponse;
import com.booking.common_library.util.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.RedisTemplate;

import javax.crypto.SecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Set;

@RestController
@RequestMapping("/test")
@Tag(name = "Test Endpoints", description = "Test endpoints for debugging")
public class TestController {

    private static final Logger log = LoggerFactory.getLogger(TestController.class);
    private final MongoTemplate mongoTemplate;
    private final RedisTemplate<String, Object> redisTemplate;

    public TestController(MongoTemplate mongoTemplate, RedisTemplate<String, Object> redisTemplate) {
        this.mongoTemplate = mongoTemplate;
        this.redisTemplate = redisTemplate;
    }

    @GetMapping("/auth")
    @Operation(summary = "Test authentication", description = "Test endpoint to verify JWT authentication")
    public ResponseEntity<ApiResponse<String>> testAuth() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String message = "Authentication successful! User: " + auth.getName() + ", Authorities: " + auth.getAuthorities();
        return ResponseEntity.ok(ApiResponse.builderResponse(SuccessCode.FETCHED, message));
    }

    @GetMapping("/decode-token")
    @Operation(summary = "Decode JWT token", description = "Decode and display JWT token contents")
    public ResponseEntity<ApiResponse<String>> decodeToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                SecretKey key = Keys.hmacShaKeyFor("in8roOCvJUSjXDse/fo4UNznTOrbw9z1Yml9wiJ/ItI/+Bxe8ju6VLLm8GamJYDajKK87lZJYBviPqAFaweV7g==".getBytes());
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(key)
                        .build()
                        .parseClaimsJws(token)
                        .getBody();
                
                String result = "Token decoded successfully!\n" +
                        "Subject: " + claims.getSubject() + "\n" +
                        "User ID: " + claims.get("userId") + "\n" +
                        "Roles: " + claims.get("roles") + "\n" +
                        "Issued At: " + claims.getIssuedAt() + "\n" +
                        "Expiration: " + claims.getExpiration();
                
                return ResponseEntity.ok(ApiResponse.builderResponse(SuccessCode.FETCHED, result));
            } else {
                return ResponseEntity.ok(ApiResponse.builderResponse(SuccessCode.FETCHED, "No valid Authorization header found"));
            }
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.builderResponse(SuccessCode.FETCHED, "Error decoding token: " + e.getMessage()));
        }
    }

    @GetMapping("/public")
    @Operation(summary = "Public test endpoint", description = "Public endpoint that doesn't require authentication")
    public ResponseEntity<ApiResponse<String>> testPublic() {
        return ResponseEntity.ok(ApiResponse.builderResponse(SuccessCode.FETCHED, "Public endpoint works!"));
    }

    @GetMapping("/simple")
    @Operation(summary = "Simple test endpoint", description = "Simple endpoint without dependencies")
    public ResponseEntity<ApiResponse<String>> testSimple() {
        return ResponseEntity.ok(ApiResponse.builderResponse(SuccessCode.FETCHED, "Simple endpoint works!"));
    }

    @GetMapping("/mongodb")
    @Operation(summary = "Test MongoDB connection", description = "Test endpoint to verify MongoDB connection and database")
    public ResponseEntity<ApiResponse<String>> testMongoDB() {
        try {
            // Test MongoDB connection
            String dbName = mongoTemplate.getDb().getName();
            long collectionCount = mongoTemplate.getDb().listCollectionNames().into(new java.util.ArrayList<>()).size();

            String result = "MongoDB Connection Test:\n" +
                    "Database Name: " + dbName + "\n" +
                    "Collections Count: " + collectionCount + "\n" +
                    "Connection Status: SUCCESS";

            log.info("MongoDB test successful: {}", result);
            return ResponseEntity.ok(ApiResponse.builderResponse(SuccessCode.FETCHED, result));

        } catch (Exception e) {
            String errorMsg = "MongoDB Connection Test FAILED: " + e.getMessage();
            log.error("MongoDB test failed", e);
            return ResponseEntity.ok(ApiResponse.builderResponse(SuccessCode.FETCHED, errorMsg));
        }
    }

    @GetMapping("/mongodb/collections")
    @Operation(summary = "List all collections", description = "List all collections in the MongoDB database")
    public ResponseEntity<ApiResponse<String>> listCollections() {
        try {
            // List all collections
            java.util.List<String> collections = mongoTemplate.getDb().listCollectionNames().into(new java.util.ArrayList<>());

            String result = "Collections in database '" + mongoTemplate.getDb().getName() + "':\n" +
                    "Total Collections: " + collections.size() + "\n" +
                    "Collections: " + String.join(", ", collections);

            log.info("Collections listed successfully: {}", result);
            return ResponseEntity.ok(ApiResponse.builderResponse(SuccessCode.FETCHED, result));

        } catch (Exception e) {
            String errorMsg = "Failed to list collections: " + e.getMessage();
            log.error("Failed to list collections", e);
            return ResponseEntity.ok(ApiResponse.builderResponse(SuccessCode.FETCHED, errorMsg));
        }
    }

    @GetMapping("/redis/test")
    @Operation(summary = "Test Redis connection", description = "Test endpoint to verify Redis connection")
    public ResponseEntity<ApiResponse<String>> testRedis() {
        try {
            // Test Redis connection
            redisTemplate.opsForValue().set("test:key", "test:value", 60, java.util.concurrent.TimeUnit.SECONDS);
            String value = (String) redisTemplate.opsForValue().get("test:key");

            String result = "Redis Connection Test:\n" +
                    "Set Key: test:key\n" +
                    "Get Value: " + value + "\n" +
                    "Connection Status: SUCCESS";

            log.info("Redis test successful: {}", result);
            return ResponseEntity.ok(ApiResponse.builderResponse(SuccessCode.FETCHED, result));

        } catch (Exception e) {
            String errorMsg = "Redis Connection Test FAILED: " + e.getMessage();
            log.error("Redis test failed", e);
            return ResponseEntity.ok(ApiResponse.builderResponse(SuccessCode.FETCHED, errorMsg));
        }
    }

        @GetMapping("/redis/clear-cache")
    @Operation(summary = "Clear Redis cache", description = "Clear all Redis cache for flights")
    public ResponseEntity<ApiResponse<String>> clearRedisCache() {
        try {
            // Clear flight-related cache
            Set<String> flightKeys = redisTemplate.keys("flight:*");
            Set<String> searchKeys = redisTemplate.keys("search:*");
            
            if (flightKeys != null && !flightKeys.isEmpty()) {
                redisTemplate.delete(flightKeys);
            }
            if (searchKeys != null && !searchKeys.isEmpty()) {
                redisTemplate.delete(searchKeys);
            }
            
            String result = "Redis Cache Cleared:\n" +
                    "Flight Keys Deleted: " + (flightKeys != null ? flightKeys.size() : 0) + "\n" +
                    "Search Keys Deleted: " + (searchKeys != null ? searchKeys.size() : 0) + "\n" +
                    "Cache Status: CLEARED";
            
            log.info("Redis cache cleared: {}", result);
            return ResponseEntity.ok(ApiResponse.builderResponse(SuccessCode.FETCHED, result));
            
        } catch (Exception e) {
            String errorMsg = "Failed to clear Redis cache: " + e.getMessage();
            log.error("Failed to clear Redis cache", e);
            return ResponseEntity.ok(ApiResponse.builderResponse(SuccessCode.FETCHED, errorMsg));
        }
    }

    @GetMapping("/redis/keys")
    @Operation(summary = "List all Redis keys", description = "List all keys currently stored in Redis")
    public ResponseEntity<ApiResponse<String>> listRedisKeys() {
        try {
            // Get all keys from Redis
            Set<String> allKeys = redisTemplate.keys("*");
            
            if (allKeys == null || allKeys.isEmpty()) {
                return ResponseEntity.ok(ApiResponse.builderResponse(SuccessCode.FETCHED, "Redis is empty - no keys found"));
            }
            
            // Get values for each key
            StringBuilder result = new StringBuilder();
            result.append("Redis Keys and Values:\n");
            result.append("Total Keys: ").append(allKeys.size()).append("\n\n");
            
            for (String key : allKeys) {
                Object value = redisTemplate.opsForValue().get(key);
                String valueStr = value != null ? value.toString() : "null";
                // Truncate long values for readability
                if (valueStr.length() > 100) {
                    valueStr = valueStr.substring(0, 100) + "... [truncated]";
                }
                result.append("Key: ").append(key).append("\n");
                result.append("Value: ").append(valueStr).append("\n");
                result.append("---\n");
            }
            
            log.info("Redis keys listed successfully: {} keys found", allKeys.size());
            return ResponseEntity.ok(ApiResponse.builderResponse(SuccessCode.FETCHED, result.toString()));
            
        } catch (Exception e) {
            String errorMsg = "Failed to list Redis keys: " + e.getMessage();
            log.error("Failed to list Redis keys", e);
            return ResponseEntity.ok(ApiResponse.builderResponse(SuccessCode.FETCHED, errorMsg));
        }
    }
}
