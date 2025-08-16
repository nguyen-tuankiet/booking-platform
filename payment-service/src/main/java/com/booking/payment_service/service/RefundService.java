package com.booking.payment_service.service;

import com.booking.payment_service.dto.request.RefundRequest;
import com.booking.payment_service.dto.respone.RefundResponse;

public interface RefundService {
    RefundResponse processRefund(RefundRequest request);
    RefundResponse getRefundStatus(String refundId);
}
