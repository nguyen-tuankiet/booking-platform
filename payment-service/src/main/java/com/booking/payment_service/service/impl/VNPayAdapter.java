package com.booking.payment_service.service.impl;

import com.booking.payment_service.dto.request.PaymentRequest;
import com.booking.payment_service.dto.respone.PaymentResponse;
import com.booking.payment_service.dto.respone.PaymentCallbackRequest;
import com.booking.payment_service.service.PaymentGatewayAdapter;
import com.booking.payment_service.service.TransactionService;
import com.booking.payment_service.utils.PaymentMethod;
import com.booking.payment_service.utils.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;

@Component("vnpay")
@RequiredArgsConstructor
@Slf4j
public class VNPayAdapter implements PaymentGatewayAdapter {

    @Value("${payment.gateway.vnpay.url}")
    private String vnpayUrl;

    @Value("${payment.gateway.vnpay.return-url}")
    private String returnUrl;

    @Value("${payment.gateway.vnpay.tmnCode:DEMO}")
    private String tmnCode;

    @Value("${payment.gateway.vnpay.hashSecret:DEMO}")
    private String hashSecret;

    private final TransactionService transactionService;

    @Override
    public PaymentResponse createPayment(PaymentRequest request, String transactionId) {
        try {
            Map<String, String> vnpParams = new TreeMap<>();
            vnpParams.put("vnp_Version", "2.1.0");
            vnpParams.put("vnp_Command", "pay");
            vnpParams.put("vnp_TmnCode", tmnCode);
            vnpParams.put("vnp_Amount", String.valueOf(request.getAmount().multiply(java.math.BigDecimal.valueOf(100)).longValue()));
            vnpParams.put("vnp_CurrCode", "VND");
            vnpParams.put("vnp_TxnRef", transactionId);
            vnpParams.put("vnp_OrderInfo", request.getDescription() != null ? request.getDescription() : "Payment for booking " + request.getBookingId());
            vnpParams.put("vnp_OrderType", "other");
            vnpParams.put("vnp_Locale", "vn");
            vnpParams.put("vnp_ReturnUrl", returnUrl);
            vnpParams.put("vnp_IpAddr", "127.0.0.1");
            vnpParams.put("vnp_CreateDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));

            // Build hash data
            StringBuilder hashData = new StringBuilder();
            StringBuilder query = new StringBuilder();

            for (Map.Entry<String, String> entry : vnpParams.entrySet()) {
                if (hashData.length() > 0) {
                    hashData.append('&');
                    query.append('&');
                }
                hashData.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8.toString()));
                hashData.append('=');
                hashData.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.toString()));

                query.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8.toString()));
                query.append('=');
                query.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.toString()));
            }

            String vnpSecureHash = hmacSHA512(hashSecret, hashData.toString());
            query.append("&vnp_SecureHash=").append(vnpSecureHash);

            String paymentUrl = vnpayUrl + "?" + query.toString();

            return PaymentResponse.builder()
                    .transactionId(transactionId)
                    .bookingId(request.getBookingId())
                    .amount(request.getAmount())
                    .currency(request.getCurrency())
                    .status(TransactionStatus.PENDING)
                    .paymentMethod(request.getPaymentMethod())
                    .paymentGateway("vnpay")
                    .paymentUrl(paymentUrl)
                    .description(request.getDescription())
                    .createdAt(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("Error creating VNPay payment", e);
            throw new RuntimeException("Failed to create VNPay payment", e);
        }
    }

    @Override
    public PaymentResponse processCallback(PaymentCallbackRequest callback) {
        String transactionId = callback.getTransactionId();
        String status = callback.getStatus();

        TransactionStatus transactionStatus = "00".equals(status) ?
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
                .paymentGateway("vnpay")
                .processedAt(LocalDateTime.now())
                .build();
    }

    @Override
    public boolean verifySignature(PaymentCallbackRequest callback) {
        try {
            Map<String, String> params = new TreeMap<>(callback.getAdditionalData());
            params.remove("vnp_SecureHash");

            StringBuilder hashData = new StringBuilder();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (hashData.length() > 0) {
                    hashData.append('&');
                }
                hashData.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8.toString()));
                hashData.append('=');
                hashData.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.toString()));
            }

            String vnpSecureHash = hmacSHA512(hashSecret, hashData.toString());
            return vnpSecureHash.equals(callback.getSignature());
        } catch (Exception e) {
            log.error("Error verifying VNPay signature", e);
            return false;
        }
    }

    @Override
    public String getGatewayName() {
        return "vnpay";
    }

    private String hmacSHA512(String key, String data) {
        try {
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac512.init(secretKey);
            byte[] result = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            for (byte b : result) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error generating HMAC SHA512", e);
        }
    }
}
