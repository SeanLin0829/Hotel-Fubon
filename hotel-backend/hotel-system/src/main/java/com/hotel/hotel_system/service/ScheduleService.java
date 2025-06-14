package com.hotel.hotel_system.service;

import com.hotel.hotel_system.DTO.ScheduleCalendarView;
import com.hotel.hotel_system.DTO.ScheduleRequest;
import com.hotel.hotel_system.DTO.ScheduleStatusSummary;
import com.hotel.hotel_system.entity.EmployeeSchedule;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public interface ScheduleService {
    EmployeeSchedule createSchedule(ScheduleRequest request);
    EmployeeSchedule getSchedule(Long id);
    List<EmployeeSchedule> getAllSchedules();
    EmployeeSchedule updateSchedule(Long id, ScheduleRequest request);
    void cancelSchedule(Long id);
    List<ScheduleCalendarView> getSchedulesByMonth(int year, int month);
}
