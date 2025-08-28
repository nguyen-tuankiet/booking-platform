package com.booking.booking_service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Value("${jwt.secret}")
    private String jwtSecret;

    private SecretKey getSigningKey() {
        // FIX: Decode the Base64-encoded secret key properly
        byte[] keyBytes = Base64.getDecoder().decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            String token = getTokenFromRequest(request);
            log.debug("Received token: {}", token != null ? token.substring(0, Math.min(20, token.length())) + "..." : "null");

            if (StringUtils.hasText(token) && validateToken(token)) {
                log.debug("Token is valid, creating authentication");
                Authentication authentication = getAuthenticationFromToken(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("Authentication set successfully for user: {}", authentication.getName());
            } else {
                log.debug("Token is invalid or empty");
            }
        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage(), e);
        }

        filterChain.doFilter(request, response);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private boolean validateToken(String token) {
        try {
            SecretKey key = getSigningKey(); // Use the fixed method
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    private Authentication getAuthenticationFromToken(String token) {
        SecretKey key = getSigningKey(); // Use the fixed method
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        String username = claims.getSubject();

        // Handle both String and Number types for userId
        Object userIdObj = claims.get("userId");
        Long userId = null;
        if (userIdObj instanceof String) {
            userId = Long.valueOf((String) userIdObj);
        } else if (userIdObj instanceof Number) {
            userId = ((Number) userIdObj).longValue();
        }

        @SuppressWarnings("unchecked")
        List<String> roles = claims.get("roles", List.class);

        List<SimpleGrantedAuthority> authorities = roles != null ? roles.stream()
                .map(role -> {
                    // Handle both formats: "ROLE_USER" and "USER"
                    if (role.startsWith("ROLE_")) {
                        return new SimpleGrantedAuthority(role);
                    } else {
                        return new SimpleGrantedAuthority("ROLE_" + role);
                    }
                })
                .collect(Collectors.toList()) : List.of();

        UserPrincipal userPrincipal = UserPrincipal.builder()
                .id(userId)
                .username(username)
                .authorities(authorities)
                .build();

        return new UsernamePasswordAuthenticationToken(userPrincipal, null, authorities);
    }
}