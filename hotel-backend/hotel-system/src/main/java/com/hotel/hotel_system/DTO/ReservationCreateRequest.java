package com.hotel.hotel_system.DTO;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ReservationCreateRequest {
    private Long userId;
    private LocalDate checkinDate;
    private LocalDate checkoutDate;
    private Integer guestCount;
    private String note;
    private List<Long> roomIds;
    // 多間房
    private String guestName;
    private String guestPhone;
}
