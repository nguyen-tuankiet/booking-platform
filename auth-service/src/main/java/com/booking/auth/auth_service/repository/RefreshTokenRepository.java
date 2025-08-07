package com.booking.auth.auth_service.repository;


import com.booking.auth.auth_service.entity.RefreshToken;
import com.booking.auth.auth_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    List<RefreshToken> findByUser(User user);

    Optional<RefreshToken> findByUserId(Long id);

    List<RefreshToken> findByUserAndRevokedAtIsNull(User user);

    @Modifying
    @Query("UPDATE RefreshToken r SET r.revokedAt = :revokedAt WHERE r.user = :user")
    void revokeAllUserTokens(@Param("user") User user, @Param("revokedAt") LocalDateTime revokedAt);

    @Modifying
    @Query("UPDATE RefreshToken r SET r.revokedAt = :revokedAt WHERE r.token = :token")
    void revokeToken(@Param("token") String token, @Param("revokedAt") LocalDateTime revokedAt);

    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.expiresAt < :currentTime OR r.revokedAt IS NOT NULL")
    void deleteExpiredAndRevokedTokens(@Param("currentTime") LocalDateTime currentTime);

    void deleteByUser(User user);
}