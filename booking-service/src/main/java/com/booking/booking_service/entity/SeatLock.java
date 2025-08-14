package com.booking.booking_service.entity;

import com.booking.booking_service.utils.LockStatus;
import com.booking.common_library.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "seat_locks")
public class SeatLock extends BaseEntity {

    @Id
    private String id;

    @Field("flight_id")
    private String flightId;

    @Field("seat_number")
    private String seatNumber;

    @Field("user_id")
    private Long userId;

    @Field("booking_id")
    private String bookingId;

    @Field("locked_at")
    private LocalDateTime lockedAt;

    @Field("expires_at")
    private LocalDateTime expiresAt;

    @Field("status")
    private LockStatus status;

    @Field("session_id")
    private String sessionId;
}
