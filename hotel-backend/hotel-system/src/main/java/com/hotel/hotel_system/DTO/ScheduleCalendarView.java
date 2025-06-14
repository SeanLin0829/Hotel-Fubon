package com.hotel.hotel_system.DTO;

import com.hotel.hotel_system.entity.EmployeeSchedule;
import com.hotel.hotel_system.entity.ShiftTemplate;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;


// 用於顯示排班日曆的資料傳輸物件，包含排班ID、員工ID、全名、班次日期、班次類型、備註和狀態。
@Data
@AllArgsConstructor
public class ScheduleCalendarView {
    private Long scheduleId;
    private Long employeeId;
    private String fullName;
    private LocalDate shiftDate;
    private ShiftTemplate.ShiftType shiftType;
    private String note;
    private EmployeeSchedule.Status status;
}
