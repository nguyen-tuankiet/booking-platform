package com.booking.booking_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatSelectionRequest {

    @NotBlank(message = "Flight ID is required")
    private String flightId;

    @NotEmpty(message = "At least one seat must be selected")
    private List<String> seatNumbers;

    private String sessionId;
}