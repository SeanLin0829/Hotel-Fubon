package com.hotel.hotel_system.DTO;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

// 用於創建餐廳預約的請求資料傳輸物件，包含用戶ID、客人姓名、電話、預約時間、客人數量、桌子ID列表和備註等信息。
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
