package com.booking.payment_service.service.impl;

import com.booking.payment_service.dto.request.PaymentRequest;
import com.booking.payment_service.dto.respone.PaymentGatewayResponse;
import com.booking.payment_service.dto.respone.PaymentResponse;
import com.booking.payment_service.dto.respone.PaymentCallbackRequest;
import com.booking.payment_service.entity.PaymentGateway;
import com.booking.payment_service.entity.Transaction;
import com.booking.payment_service.repository.PaymentGatewayRepository;
import com.booking.payment_service.service.PaymentGatewayAdapter;
import com.booking.payment_service.service.PaymentGatewayService;

import com.booking.payment_service.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentGatewayServiceImpl implements PaymentGatewayService {

    private final PaymentGatewayRepository gatewayRepository;
    private final TransactionService transactionService;
    private final Map<String, PaymentGatewayAdapter> gatewayAdapters;

    @Override
    public List<PaymentGatewayResponse> getAvailableGateways(BigDecimal amount, String currency) {
        List<PaymentGateway> gateways = gatewayRepository.findAvailableGateways(amount, currency);

        return gateways.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public PaymentResponse createPayment(PaymentRequest request) {
        // Find suitable gateway
        String gatewayName = Optional.ofNullable(request.getPaymentGateway())
                .orElse(findBestGateway(request.getAmount(), request.getCurrency()));

        PaymentGatewayAdapter adapter = gatewayAdapters.get(gatewayName);
        if (adapter == null) {
            throw new RuntimeException("Payment gateway not supported: " + gatewayName);
        }

        // Create transaction record
        Transaction transaction = transactionService.createTransaction(request, gatewayName);

        try {
            // Process payment through gateway
            PaymentResponse response = adapter.createPayment(request, transaction.getTransactionId());

            // Update transaction with gateway response
            transactionService.updateTransactionWithGatewayResponse(
                    transaction.getTransactionId(),
                    response.getPaymentUrl(),
                    null
            );

            return response;
        } catch (Exception e) {
            log.error("Payment creation failed for transaction: {}", transaction.getTransactionId(), e);
            transactionService.markTransactionFailed(transaction.getTransactionId(), e.getMessage());
            throw new RuntimeException("Payment creation failed", e);
        }
    }

    @Override
    public PaymentResponse processCallback(String gateway, PaymentCallbackRequest callback) {
        PaymentGatewayAdapter adapter = gatewayAdapters.get(gateway);
        if (adapter == null) {
            throw new RuntimeException("Payment gateway not supported: " + gateway);
        }

        return adapter.processCallback(callback);
    }

    @Override
    public boolean verifySignature(String gateway, PaymentCallbackRequest callback) {
        PaymentGatewayAdapter adapter = gatewayAdapters.get(gateway);
        if (adapter == null) {
            return false;
        }

        return adapter.verifySignature(callback);
    }

    private String findBestGateway(BigDecimal amount, String currency) {
        List<PaymentGateway> gateways = gatewayRepository.findAvailableGateways(amount, currency);

        if (gateways.isEmpty()) {
            throw new RuntimeException("No available payment gateway for amount: " + amount);
        }

        // Return the highest priority gateway
        return gateways.get(0).getName();
    }

    private PaymentGatewayResponse mapToResponse(PaymentGateway gateway) {
        return PaymentGatewayResponse.builder()
                .name(gateway.getName())
                .displayName(gateway.getDisplayName())
                .isEnabled(gateway.getIsEnabled())
                .minAmount(gateway.getMinAmount())
                .maxAmount(gateway.getMaxAmount())
                .supportedCurrencies(gateway.getSupportedCurrencies() != null ?
                        gateway.getSupportedCurrencies().split(",") : new String[]{"VND"})
                .priorityOrder(gateway.getPriorityOrder())
                .build();
    }
}
