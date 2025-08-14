package com.booking.payment_service.entity;

import com.booking.common_library.entity.BaseEntity;
import com.booking.payment_service.utils.PaymentMethod;
import com.booking.payment_service.utils.TransactionStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "transactions")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Transaction extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "transaction_id", unique = true, nullable = false)
    String transactionId;

    @Column(name = "booking_id", nullable = false)
    Long bookingId;

    @Column(name = "user_id", nullable = false)
    Long userId;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    BigDecimal amount;

    @Column(name = "currency", nullable = false)
    String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    TransactionStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    PaymentMethod paymentMethod;

    @Column(name = "payment_gateway")
    String paymentGateway;

    @Column(name = "gateway_transaction_id")
    String gatewayTransactionId;

    @Column(name = "gateway_response")
    @Lob
    String gatewayResponse;

    @Column(name = "description")
    String description;

    @Column(name = "ip_address")
    String ipAddress;

    @Column(name = "user_agent")
    String userAgent;

    @Column(name = "processed_at")
    LocalDateTime processedAt;

    @Column(name = "failed_reason")
    String failedReason;

    @Column(name = "retry_count")
    Integer retryCount = 0;

    @Column(name = "next_retry_at")
    LocalDateTime nextRetryAt;

    @Column(name = "is_priority", nullable = false)
    Boolean isPriority = false;

    @Column(name = "otp_verified", nullable = false)
    Boolean otpVerified = false;
}
