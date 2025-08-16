package com.booking.payment_service.service.impl;

import com.booking.payment_service.dto.request.PaymentRequest;
import com.booking.payment_service.dto.respone.PaymentResponse;
import com.booking.payment_service.dto.respone.PaymentCallbackRequest;
import com.booking.payment_service.service.PaymentGatewayAdapter;
import com.booking.payment_service.service.TransactionService;
import com.booking.payment_service.utils.TransactionStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component("momo")
@RequiredArgsConstructor
@Slf4j
public class MoMoAdapter implements PaymentGatewayAdapter {

    @Value("${payment.gateway.momo.url}")
    private String momoUrl;

    @Value("${payment.gateway.momo.return-url}")
    private String returnUrl;

    @Value("${payment.gateway.momo.partner-code:DEMO}")
    private String partnerCode;

    @Value("${payment.gateway.momo.access-key:DEMO}")
    private String accessKey;

    @Value("${payment.gateway.momo.secret-key:DEMO}")
    private String secretKey;

    private final TransactionService transactionService;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public PaymentResponse createPayment(PaymentRequest request, String transactionId) {
        try {
            String requestId = UUID.randomUUID().toString();
            String orderId = transactionId;
            String orderInfo = request.getDescription() != null ? request.getDescription() : "Payment for booking " + request.getBookingId();
            String amount = String.valueOf(request.getAmount().longValue());
            String extraData = "";
            String requestType = "payWithMethod";
            String notifyUrl = returnUrl; // In production, should be different

            // Create signature
            String rawHash = "accessKey=" + accessKey +
                    "&amount=" + amount +
                    "&extraData=" + extraData +
                    "&ipnUrl=" + returnUrl +
                    "&orderId=" + orderId +
                    "&orderInfo=" + orderInfo +
                    "&partnerCode=" + partnerCode +
                    "&redirectUrl=" + returnUrl +
                    "&requestId=" + requestId +
                    "&requestType=" + requestType;

            String signature = hmacSHA256(secretKey, rawHash);

            // Create request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("partnerCode", partnerCode);
            requestBody.put("partnerName", "Booking Service");
            requestBody.put("storeId", "BookingStore");
            requestBody.put("requestId", requestId);
            requestBody.put("amount", Long.parseLong(amount));
            requestBody.put("orderId", orderId);
            requestBody.put("orderInfo", orderInfo);
            requestBody.put("redirectUrl", returnUrl);
            requestBody.put("ipnUrl", returnUrl);
            requestBody.put("lang", "vi");
            requestBody.put("extraData", extraData);
            requestBody.put("requestType", requestType);
            requestBody.put("signature", signature);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // Mock response for demo
            String paymentUrl = "https://test-payment.momo.vn/v2/gateway/pay?t=" + transactionId;

            return PaymentResponse.builder()
                    .transactionId(transactionId)
                    .bookingId(request.getBookingId())
                    .amount(request.getAmount())
                    .currency(request.getCurrency())
                    .status(TransactionStatus.PENDING)
                    .paymentMethod(request.getPaymentMethod())
                    .paymentGateway("momo")
                    .paymentUrl(paymentUrl)
                    .description(orderInfo)
                    .createdAt(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("Error creating MoMo payment", e);
            throw new RuntimeException("Failed to create MoMo payment", e);
        }
    }

    @Override
    public PaymentResponse processCallback(PaymentCallbackRequest callback) {
        String transactionId = callback.getTransactionId();
        String resultCode = callback.getStatus();

        TransactionStatus transactionStatus = "0".equals(resultCode) ?
                TransactionStatus.SUCCESS : TransactionStatus.FAILED;

        // Update transaction
        transactionService.updateTransactionStatus(transactionId, transactionStatus,
                callback.getGatewayTransactionId());

        // Get updated transaction
        var transaction = transactionService.getTransactionByIdOrThrow(transactionId);

        return PaymentResponse.builder()
                .transactionId(transactionId)
                .bookingId(transaction.getBookingId())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .status(transactionStatus)
                .paymentMethod(transaction.getPaymentMethod())
                .paymentGateway("momo")
                .processedAt(LocalDateTime.now())
                .build();
    }

    @Override
    public boolean verifySignature(PaymentCallbackRequest callback) {
        try {
            Map<String, String> params = callback.getAdditionalData();
            String signature = callback.getSignature();

            // Build raw hash for verification
            String rawHash = "accessKey=" + accessKey +
                    "&amount=" + params.get("amount") +
                    "&extraData=" + params.getOrDefault("extraData", "") +
                    "&message=" + params.getOrDefault("message", "") +
                    "&orderId=" + params.get("orderId") +
                    "&orderInfo=" + params.get("orderInfo") +
                    "&orderType=" + params.getOrDefault("orderType", "") +
                    "&partnerCode=" + partnerCode +
                    "&payType=" + params.getOrDefault("payType", "") +
                    "&requestId=" + params.get("requestId") +
                    "&responseTime=" + params.get("responseTime") +
                    "&resultCode=" + params.get("resultCode") +
                    "&transId=" + params.getOrDefault("transId", "");

            String expectedSignature = hmacSHA256(secretKey, rawHash);
            return expectedSignature.equals(signature);
        } catch (Exception e) {
            log.error("Error verifying MoMo signature", e);
            return false;
        }
    }

    @Override
    public String getGatewayName() {
        return "momo";
    }

    private String hmacSHA256(String key, String data) {
        try {
            Mac hmac256 = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            hmac256.init(secretKey);
            byte[] result = hmac256.doFinal(data.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            for (byte b : result) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error generating HMAC SHA256", e);
        }
    }
}
