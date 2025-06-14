package com.hotel.hotel_system.DTO;

import com.hotel.hotel_system.entity.EmployeeSchedule;
import com.hotel.hotel_system.entity.ShiftTemplate;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

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
