package com.hotel.hotel_system.DTO;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

// 用於創建新預訂的請求資料傳輸物件，包含用戶ID、入住和退房日期、客人數量、備註、房間ID列表等信息。
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
