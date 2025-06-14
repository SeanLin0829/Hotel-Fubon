package com.hotel.hotel_system.DTO;


import com.hotel.hotel_system.entity.EmployeeSchedule;
import com.hotel.hotel_system.entity.ShiftTemplate;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ScheduleRequest {
    private Long employeeId;
    private LocalDate shiftDate;
    private ShiftTemplate.ShiftType shiftType;
    private String note;
    private EmployeeSchedule.Status status; // 加上 status 欄位
}