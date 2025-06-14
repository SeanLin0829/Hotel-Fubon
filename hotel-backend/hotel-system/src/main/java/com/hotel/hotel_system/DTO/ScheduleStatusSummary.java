package com.hotel.hotel_system.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;


// 用於顯示排班狀態摘要的資料傳輸物件，包含日期和總排班數量。
@Data
@AllArgsConstructor
public class ScheduleStatusSummary {
    private LocalDate date;
    private long totalScheduled;
}
