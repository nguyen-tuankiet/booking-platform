package com.booking.payment_service.saga;

import com.booking.payment_service.utils.StepStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SagaStep {
    private String stepId;
    private String stepName;
    private StepStatus status;
    private String serviceEndpoint;
    private Map<String, Object> requestData;
    private Map<String, Object> responseData;
    private String compensationEndpoint;
    private Map<String, Object> compensationData;
    private LocalDateTime executedAt;
    private String failureReason;
}
