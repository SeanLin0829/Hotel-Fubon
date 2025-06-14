package com.hotel.hotel_system.DTO;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class RestaurantReservationCreateRequest {
    private Long userId; // 可為 null，表示匿名預約
    private String guestName; // 匿名預約時填寫
    private String guestPhone; // 匿名預約時填寫
    private LocalDateTime reservationTime;
    private int numberOfGuests;
    private List<Long> tableIds;
    private String note;
}
