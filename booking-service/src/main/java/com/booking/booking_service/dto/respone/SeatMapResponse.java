package com.booking.booking_service.dto.respone;
import com.booking.booking_service.entity.SeatLegend;
import com.booking.booking_service.entity.SeatRow;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatMapResponse {
    private String flightId;
    private String aircraftType;
    private List<SeatRow> seatRows;
    private SeatLegend legend;  // Chú thích màu/trạng thái ghế
}
