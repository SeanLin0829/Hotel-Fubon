package com.hotel.hotel_system.repository;

import com.hotel.hotel_system.entity.EmployeeSchedule;
import com.hotel.hotel_system.entity.ShiftTemplate;
import com.hotel.hotel_system.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeScheduleRepository extends JpaRepository<EmployeeSchedule, Long> {

    /**
     * 查詢某員工在特定日期、特定班別是否已有排班（用於避免重複排班）。
     */
    Optional<EmployeeSchedule> findByEmployeeAndShiftDateAndShiftType(User employee, LocalDate shiftDate, ShiftTemplate.ShiftType shiftType);

    List<EmployeeSchedule> findByEmployeeAndShiftDate(User employee, LocalDate shiftDate);

    /**
     * 查詢某員工在某段日期區間內的所有排班（用於檢查連續排班天數）。
     */
    List<EmployeeSchedule> findByEmployeeAndShiftDateBetween(User employee, LocalDate start, LocalDate end);

    /**
     * 查詢特定日期的所有排班資料（用於顯示當日班表）。
     */
    List<EmployeeSchedule> findByShiftDate(LocalDate date);

    /**
     * 查詢某員工的所有排班資料。
     */
    List<EmployeeSchedule> findByEmployee(User employee);

    List<EmployeeSchedule> findByShiftDateBetween(LocalDate start, LocalDate end);
}
