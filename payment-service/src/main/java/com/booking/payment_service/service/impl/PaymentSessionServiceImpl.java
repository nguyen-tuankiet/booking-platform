package com.booking.payment_service.service.impl;

import com.booking.common_library.exception.ResourceNotFoundException;
import com.booking.payment_service.dto.request.PaymentRequest;
import com.booking.payment_service.entity.PaymentSession;
import com.booking.payment_service.repository.PaymentSessionRepository;
import com.booking.payment_service.service.PaymentSessionService;
import com.booking.payment_service.utils.PaymentSessionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PaymentSessionServiceImpl implements PaymentSessionService {

    private final PaymentSessionRepository sessionRepository;

    @Override
    public PaymentSession createPaymentSession(PaymentRequest request) {
        String sessionId = generateSessionId();

        PaymentSession session = PaymentSession.builder()
                .sessionId(sessionId)
                .bookingId(request.getBookingId())
                .userId(getCurrentUserId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .status(PaymentSessionStatus.CREATED)
                .paymentGateway(request.getPaymentGateway())
                .expiresAt(LocalDateTime.now().plusMinutes(15)) // 15 minutes expiry
                .build();

        return sessionRepository.save(session);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentSession getSessionById(String sessionId) {
        return sessionRepository.findBySessionIdAndDeleted(sessionId, false)
                .orElseThrow(() -> new ResourceNotFoundException("Payment session not found: " + sessionId));
    }

    @Override
    public void expireOldSessions() {
        int expiredCount = sessionRepository.expireOldSessions(LocalDateTime.now());
        if (expiredCount > 0) {
            log.info("Expired {} old payment sessions", expiredCount);
        }
    }

    private String generateSessionId() {
        return "PS" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private Long getCurrentUserId() {
        // In a real application, this would come from SecurityContextHolder
        return 1L;
    }
}
