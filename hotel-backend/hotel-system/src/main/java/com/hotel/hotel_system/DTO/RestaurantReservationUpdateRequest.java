package com.hotel.hotel_system.DTO;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class RestaurantReservationUpdateRequest {
    private LocalDateTime reservationTime; // 新的訂位時間
    private int numberOfGuests;            // 修改後的人數
    private List<Long> tableIds;           // 修改後的桌位 ID 清單
    private String note;

    private String guestName;
    private String guestPhone;
}
