package com.booking.payment_service.repository;


import com.booking.payment_service.entity.PaymentGateway;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentGatewayRepository extends JpaRepository<PaymentGateway, Long> {

    Optional<PaymentGateway> findByNameAndDeleted(String name, Boolean deleted);

    List<PaymentGateway> findByIsEnabledAndDeletedOrderByPriorityOrderAsc(Boolean isEnabled, Boolean deleted);

    @Query("SELECT pg FROM PaymentGateway pg WHERE pg.isEnabled = true AND pg.deleted = false AND " +
            "(:amount >= pg.minAmount OR pg.minAmount IS NULL) AND " +
            "(:amount <= pg.maxAmount OR pg.maxAmount IS NULL) AND " +
            "(pg.supportedCurrencies IS NULL OR pg.supportedCurrencies LIKE %:currency%) " +
            "ORDER BY pg.priorityOrder ASC")
    List<PaymentGateway> findAvailableGateways(@Param("amount") BigDecimal amount,
                                               @Param("currency") String currency);
}
