package com.booking.booking_service.service;

import com.booking.booking_service.dto.request.BookingRequest;
import com.booking.booking_service.dto.request.SeatSelectionRequest;
import com.booking.booking_service.dto.respone.BookingResponse;
import com.booking.booking_service.entity.SeatLock;
import com.booking.booking_service.utils.PaymentStatus;
import com.booking.common_library.dto.PageResponse;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface BookingService {
    BookingResponse createBooking(BookingRequest request);
    List<SeatLock> selectSeats(SeatSelectionRequest request);
    PageResponse<BookingResponse> getUserBookings(Pageable pageable);
    BookingResponse getBookingByReference(String bookingReference);
    void cancelBooking(String bookingId, String reason);
    void confirmBooking(String bookingId);
    void expireBooking(String bookingId);
    BookingResponse getBookingById(String bookingId);
    void updateBookingPaymentStatus(String bookingId, PaymentStatus paymentStatus);

}
