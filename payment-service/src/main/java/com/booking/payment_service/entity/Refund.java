package com.booking.payment_service.entity;

import com.booking.common_library.entity.BaseEntity;
import com.booking.payment_service.utils.RefundStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "refunds")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Refund extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "refund_id", unique = true, nullable = false)
    String refundId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    Transaction transaction;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    RefundStatus status;

    @Column(name = "reason")
    String reason;

    @Column(name = "gateway_refund_id")
    String gatewayRefundId;

    @Column(name = "gateway_response")
    @Lob
    String gatewayResponse;

    @Column(name = "processed_at")
    LocalDateTime processedAt;

    @Column(name = "failed_reason")
    String failedReason;
}
