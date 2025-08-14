package com.booking.payment_service.entity;

import com.booking.common_library.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "payment_gateways")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentGateway extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "name", unique = true, nullable = false)
    String name;

    @Column(name = "display_name", nullable = false)
    String displayName;

    @Column(name = "is_enabled", nullable = false)
    Boolean isEnabled = true;

    @Column(name = "api_url")
    String apiUrl;

    @Column(name = "return_url")
    String returnUrl;

    @Column(name = "config_json")
    @Lob
    String configJson;

    @Column(name = "priority_order")
    Integer priorityOrder = 0;

    @Column(name = "min_amount", precision = 19, scale = 2)
    java.math.BigDecimal minAmount;

    @Column(name = "max_amount", precision = 19, scale = 2)
    java.math.BigDecimal maxAmount;

    @Column(name = "supported_currencies")
    String supportedCurrencies;
}