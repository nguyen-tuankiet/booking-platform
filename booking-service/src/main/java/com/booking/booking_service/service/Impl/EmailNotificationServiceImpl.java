package com.booking.booking_service.service.Impl;

import com.booking.booking_service.entity.Booking;
import com.booking.booking_service.entity.Flight;
import com.booking.booking_service.entity.PassengerInfo;
import com.booking.booking_service.service.EmailNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationServiceImpl implements EmailNotificationService {

    private final RestTemplate restTemplate;

    @Value("${services.auth-service.url:http://localhost:8081}")
    private String authServiceUrl;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public void sendBookingConfirmationEmail(Booking booking, Flight flight) {
        try {
            String toEmail = getUserEmail(booking);
            String firstName = getUserFirstName(booking);

            if (toEmail == null || firstName == null) {
                log.warn("Cannot send booking confirmation email - missing contact information for booking: {}",
                        booking.getBookingReference());
                return;
            }

            String route = flight.getDepartureAirport() + " → " + flight.getArrivalAirport();
            String departureTime = flight.getDepartureTime().format(DATE_TIME_FORMATTER);
            String seatNumbers = String.join(", ", booking.getSelectedSeats());

            Map<String, String> emailData = new HashMap<>();
            emailData.put("toEmail", toEmail);
            emailData.put("firstName", firstName);
            emailData.put("bookingReference", booking.getBookingReference());
            emailData.put("flightNumber", flight.getFlightNumber());
            emailData.put("route", route);
            emailData.put("departureTime", departureTime);
            emailData.put("totalAmount", booking.getTotalAmount().toString());
            emailData.put("seatNumbers", seatNumbers);

            callEmailService("/api/emails/booking-confirmation", emailData);
            log.info("Booking confirmation email triggered for booking: {} to email: {}",
                    booking.getBookingReference(), toEmail);

        } catch (Exception e) {
            log.error("Failed to send booking confirmation email for booking: {}",
                    booking.getBookingReference(), e);
        }
    }

    @Override
    public void sendBookingCancellationEmail(Booking booking, Flight flight, String cancellationReason) {
        try {
            String toEmail = getUserEmail(booking);
            String firstName = getUserFirstName(booking);

            if (toEmail == null || firstName == null) {
                log.warn("Cannot send booking cancellation email - missing contact information for booking: {}",
                        booking.getBookingReference());
                return;
            }

            String route = flight.getDepartureAirport() + " → " + flight.getArrivalAirport();
            String departureTime = flight.getDepartureTime().format(DATE_TIME_FORMATTER);

            Map<String, String> emailData = new HashMap<>();
            emailData.put("toEmail", toEmail);
            emailData.put("firstName", firstName);
            emailData.put("bookingReference", booking.getBookingReference());
            emailData.put("flightNumber", flight.getFlightNumber());
            emailData.put("route", route);
            emailData.put("departureTime", departureTime);
            emailData.put("cancellationReason", cancellationReason);

            callEmailService("/api/emails/booking-cancellation", emailData);
            log.info("Booking cancellation email triggered for booking: {} to email: {}",
                    booking.getBookingReference(), toEmail);

        } catch (Exception e) {
            log.error("Failed to send booking cancellation email for booking: {}",
                    booking.getBookingReference(), e);
        }
    }

    /**
     * Get user email from booking contact information
     */
    private String getUserEmail(Booking booking) {
        if (booking.getContactInfo() != null && booking.getContactInfo().getEmail() != null) {
            return booking.getContactInfo().getEmail();
        }
        
        // Fallback: try to get email from first passenger if available
        if (booking.getPassengers() != null && !booking.getPassengers().isEmpty()) {
            // Note: This is a fallback, ideally we should have contact info
            log.warn("Using fallback email from passenger info for booking: {}", 
                    booking.getBookingReference());
        }
        
        return null;
    }

    /**
     * Get user first name from booking passenger information
     */
    private String getUserFirstName(Booking booking) {
        if (booking.getPassengers() != null && !booking.getPassengers().isEmpty()) {
            PassengerInfo firstPassenger = booking.getPassengers().get(0);
            if (firstPassenger.getFirstName() != null) {
                return firstPassenger.getFirstName();
            }
        }
        
        log.warn("Cannot find passenger first name for booking: {}", 
                booking.getBookingReference());
        return null;
    }

    /**
     * Call external email service to send emails
     */
    private void callEmailService(String endpoint, Map<String, String> emailData) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, String>> request = new HttpEntity<>(emailData, headers);
            
            String emailServiceUrl = authServiceUrl + endpoint;
            
            restTemplate.exchange(
                emailServiceUrl,
                HttpMethod.POST,
                request,
                String.class
            );
            
            log.debug("Email service called successfully at endpoint: {}", endpoint);
            
        } catch (Exception e) {
            log.error("Failed to call email service at endpoint: {}", endpoint, e);
            // Note: We don't throw the exception here to avoid breaking the main flow
            // The calling method will handle the error logging
        }
    }
}