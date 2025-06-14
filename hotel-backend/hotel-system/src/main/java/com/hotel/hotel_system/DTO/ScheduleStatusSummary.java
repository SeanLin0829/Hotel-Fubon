package com.hotel.hotel_system.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class ScheduleStatusSummary {
    private LocalDate date;
    private long totalScheduled;
}
