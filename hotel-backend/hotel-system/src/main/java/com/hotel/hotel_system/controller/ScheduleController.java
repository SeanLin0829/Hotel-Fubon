package com.hotel.hotel_system.controller;

import com.hotel.hotel_system.DTO.ScheduleCalendarView;
import com.hotel.hotel_system.DTO.ScheduleRequest;
import com.hotel.hotel_system.DTO.ScheduleStatusSummary;
import com.hotel.hotel_system.entity.EmployeeSchedule;
import com.hotel.hotel_system.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    // 新增排班
    @PostMapping
    public ResponseEntity<EmployeeSchedule> createSchedule(@RequestBody ScheduleRequest request) {
        EmployeeSchedule created = scheduleService.createSchedule(request);
        return ResponseEntity.ok(created);
    }

    // 查詢單筆排班
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeSchedule> getSchedule(@PathVariable Long id) {
        return ResponseEntity.ok(scheduleService.getSchedule(id));
    }

    // 查詢所有排班
    @GetMapping
    public ResponseEntity<List<EmployeeSchedule>> getAllSchedules() {
        return ResponseEntity.ok(scheduleService.getAllSchedules());
    }

    // 更新排班
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeSchedule> updateSchedule(@PathVariable Long id, @RequestBody ScheduleRequest request) {
        return ResponseEntity.ok(scheduleService.updateSchedule(id, request));
    }

    // 取消排班
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelSchedule(@PathVariable Long id) {
        scheduleService.cancelSchedule(id);
        return ResponseEntity.noContent().build();
    }

    // 取得某月排班日曆視圖
    @GetMapping("/monthly")
    public ResponseEntity<List<ScheduleCalendarView>> getSchedulesByMonth(
            @RequestParam int year,
            @RequestParam int month
    ) {
        return ResponseEntity.ok(scheduleService.getSchedulesByMonth(year, month));
    }

}