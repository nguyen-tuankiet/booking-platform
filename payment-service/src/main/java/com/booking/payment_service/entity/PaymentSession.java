package com.booking.payment_service.entity;

import com.booking.common_library.entity.BaseEntity;
import com.booking.payment_service.utils.PaymentSessionStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_sessions")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentSession extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "session_id", unique = true, nullable = false)
    String sessionId;

    @Column(name = "booking_id", nullable = false)
    String bookingId;

    @Column(name = "user_id", nullable = false)
    Long userId;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    BigDecimal amount;

    @Column(name = "currency", nullable = false)
    String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    PaymentSessionStatus status;

    @Column(name = "payment_gateway", nullable = false)
    String paymentGateway;

    @Column(name = "gateway_session_id")
    String gatewaySessionId;

    @Column(name = "payment_url")
    String paymentUrl;

    @Column(name = "expires_at", nullable = false)
    LocalDateTime expiresAt;

    @Column(name = "ip_address")
    String ipAddress;

    @Column(name = "user_agent")
    String userAgent;

    @Column(name = "metadata")
    @Lob
    String metadata;
}