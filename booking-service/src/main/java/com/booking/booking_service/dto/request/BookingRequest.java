package com.booking.booking_service.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequest {

    @NotBlank(message = "Flight ID is required")
    private String flightId;

    @NotEmpty(message = "At least one passenger is required")
    @Valid
    private List<PassengerRequest> passengers;

    @NotEmpty(message = "At least one seat must be selected")
    private List<String> selectedSeats;

    @NotBlank(message = "Seat class is required")
    private String seatClass;

    @Valid
    @NotNull(message = "Contact information is required")
    private ContactRequest contactInfo;

    private String specialRequests;

    private String promoCode;

}