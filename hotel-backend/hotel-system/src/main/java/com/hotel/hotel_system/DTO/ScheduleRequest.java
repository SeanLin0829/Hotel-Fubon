package com.hotel.hotel_system.DTO;


import com.hotel.hotel_system.entity.EmployeeSchedule;
import com.hotel.hotel_system.entity.ShiftTemplate;
import lombok.Data;

import java.time.LocalDate;

// 用於創建新排班的請求資料傳輸物件，包含員工ID、班次日期、班次類型、備註和狀態。
@Data
public class ScheduleRequest {
    private Long employeeId;
    private LocalDate shiftDate;
    private ShiftTemplate.ShiftType shiftType;
    private String note;
    private EmployeeSchedule.Status status; // 加上 status 欄位
}