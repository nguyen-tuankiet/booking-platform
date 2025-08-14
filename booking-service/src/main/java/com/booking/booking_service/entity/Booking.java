package com.booking.booking_service.entity;


import com.booking.booking_service.utils.BookingStatus;
import com.booking.booking_service.utils.PaymentStatus;
import com.booking.common_library.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "bookings")
public class Booking extends BaseEntity {
    @Id
    private String id;

    @Field("booking_reference")
    private String bookingReference; // Mã đặt vé duy nhất (thường gửi cho khách)

    @Field("user_id")
    private Long userId; // ID của người dùng đã đặt vé

    @Field("flight_id")
    private String flightId; // ID chuyến bay đã đặt

    @Field("passengers")
    private List<PassengerInfo> passengers; // Danh sách hành khách

    @Field("selected_seats")
    private List<String> selectedSeats; // Danh sách số ghế đã chọn

    @Field("seat_class")
    private String seatClass; // Hạng ghế (Economy, Business, First)

    @Field("total_amount")
    private BigDecimal totalAmount; // Tổng số tiền cần thanh toán

    @Field("booking_status")
    private BookingStatus bookingStatus; // Trạng thái đặt vé

    @Field("payment_status")
    private PaymentStatus paymentStatus; // Trạng thái thanh toán

    @Field("contact_info")
    private ContactInfo contactInfo; // Thông tin liên hệ chính

    @Field("special_requests")
    private String specialRequests; // Yêu cầu đặc biệt (ăn chay, hỗ trợ xe lăn...)

    @Field("lock_expires_at")
    private LocalDateTime lockExpiresAt; // Thời điểm hết hạn giữ ghế

    @Field("confirmed_at")
    private LocalDateTime confirmedAt; // Thời điểm xác nhận đặt vé

    @Field("cancelled_at")
    private LocalDateTime cancelledAt; // Thời điểm hủy vé

    @Field("cancellation_reason")
    private String cancellationReason; // Lý do hủy vé

}
