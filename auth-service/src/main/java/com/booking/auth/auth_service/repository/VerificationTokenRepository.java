package com.booking.auth.auth_service.repository;

import com.booking.auth.auth_service.entity.User;
import com.booking.auth.auth_service.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

    Optional<VerificationToken> findByToken(String token);

    Optional<VerificationToken> findByUserAndType(User user, VerificationToken.TokenType type);

    List<VerificationToken> findByUserAndTypeAndUsedAtIsNull(User user, VerificationToken.TokenType type);

    @Modifying
    @Query("DELETE FROM VerificationToken v WHERE v.expiresAt < :currentTime")
    void deleteExpiredTokens(@Param("currentTime") LocalDateTime currentTime);

    @Modifying
    @Query("UPDATE VerificationToken v SET v.usedAt = :usedAt WHERE v.token = :token")
    void markTokenAsUsed(@Param("token") String token, @Param("usedAt") LocalDateTime usedAt);

    void deleteByUser(User user);

    @Modifying
    @Query("DELETE FROM VerificationToken v WHERE v.user = :user AND v.type = :type")
    void deleteByUserAndType(@Param("user") User user, @Param("type") VerificationToken.TokenType type);
}