package com.hotel.hotel_system.service.serviceImpl;

import com.hotel.hotel_system.DTO.ScheduleCalendarView;
import com.hotel.hotel_system.DTO.ScheduleRequest;
import com.hotel.hotel_system.DTO.ScheduleStatusSummary;
import com.hotel.hotel_system.entity.EmployeeSchedule;
import com.hotel.hotel_system.entity.ShiftTemplate;
import com.hotel.hotel_system.entity.User;
import com.hotel.hotel_system.repository.EmployeeScheduleRepository;
import com.hotel.hotel_system.repository.ShiftTemplateRepository;
import com.hotel.hotel_system.repository.UserRepository;
import com.hotel.hotel_system.service.ScheduleService;
import com.hotel.hotel_system.util.DateUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {

    private final EmployeeScheduleRepository scheduleRepo;
    private final ShiftTemplateRepository shiftTemplateRepo;
    private final UserRepository userRepo;

    @Override
    @Transactional
    public EmployeeSchedule createSchedule(ScheduleRequest request) {
        User employee = userRepo.findById(request.getEmployeeId())
                .orElseThrow(() -> new IllegalArgumentException("找不到員工"));
        if (employee.getRole().getId() != 2) {
            throw new IllegalArgumentException("僅限員工（staff）可以被排班");
        }
        scheduleRepo.findByEmployeeAndShiftDateAndShiftType(employee, request.getShiftDate(), request.getShiftType())
                .ifPresent(s -> {
                    throw new IllegalArgumentException("此員工在該日已排此班別，請勿重複排班");
                });

        ShiftTemplate shiftTemplate = shiftTemplateRepo.findByShiftType(request.getShiftType())
                .orElseThrow(() -> new IllegalArgumentException("無此班別定義"));

        validateConsecutiveDays(employee, request.getShiftDate(), request.getShiftType());
        validateShiftSequence(employee, request.getShiftDate(), request.getShiftType()); // <--- 新增

        EmployeeSchedule schedule = EmployeeSchedule.builder()
                .employee(employee)
                .shiftDate(request.getShiftDate())
                .shiftType(request.getShiftType())
                .status(EmployeeSchedule.Status.scheduled)
                .note(request.getNote())
                .createdAt(LocalDateTime.now())
                .build();

        return scheduleRepo.save(schedule);
    }

    private void validateRestTime(User employee, LocalDate newDate, ShiftTemplate newShift) {
        List<EmployeeSchedule> previousSchedules = scheduleRepo.findByEmployeeAndShiftDateBetween(
                employee, newDate.minusDays(1), newDate);

        LocalDateTime newStart = LocalDateTime.of(newDate, newShift.getStartTime());
        if (newShift.isCrossDay()) {
            newStart = newStart.minusDays(1);
        }

        for (EmployeeSchedule prev : previousSchedules) {
            ShiftTemplate prevTemplate = shiftTemplateRepo.findByShiftType(prev.getShiftType())
                    .orElse(null);
            if (prevTemplate == null) continue;

            LocalDateTime prevEnd = LocalDateTime.of(prev.getShiftDate(), prevTemplate.getEndTime());
            if (prevTemplate.isCrossDay()) {
                prevEnd = prevEnd.plusDays(1);
            }

            Duration gap = Duration.between(prevEnd, newStart);
            if (!gap.isNegative() && gap.toHours() < 12) {
                throw new IllegalArgumentException("排班間隔須至少 12 小時");
            }
        }
    }

    private void validateConsecutiveDays(User employee, LocalDate newDate, ShiftTemplate.ShiftType newType) {
        if (newType == ShiftTemplate.ShiftType.off) return; // 休假直接 return

        LocalDate start = newDate.minusDays(10);
        LocalDate end = newDate.plusDays(10);
        List<EmployeeSchedule> allSchedules = scheduleRepo.findByEmployeeAndShiftDateBetween(employee, start, end);

        List<LocalDate> workDates = allSchedules.stream()
                .filter(s -> s.getShiftType() != ShiftTemplate.ShiftType.off)
                .map(EmployeeSchedule::getShiftDate)
                .collect(Collectors.toList());

        // 新班非休假才加入
        workDates.add(newDate);

        workDates = workDates.stream().distinct().sorted().collect(Collectors.toList());

        int continuous = 1, maxContinuous = 1;
        for (int i = 1; i < workDates.size(); i++) {
            if (workDates.get(i-1).plusDays(1).equals(workDates.get(i))) {
                continuous++;
                maxContinuous = Math.max(maxContinuous, continuous);
            } else {
                continuous = 1;
            }
        }
        if (maxContinuous > 6) {
            throw new IllegalArgumentException("不得連續工作超過6天，請排休");
        }
    }

    private void validateShiftSequence(User employee, LocalDate newDate, ShiftTemplate.ShiftType newType) {
        // 新班是休假，完全不檢查
        if (newType == ShiftTemplate.ShiftType.off) return;

        // 1. 前一天
        Optional<EmployeeSchedule> prevOpt = scheduleRepo.findByEmployeeAndShiftDate(employee, newDate.minusDays(1))
                .stream().findFirst();
        if (prevOpt.isPresent()) {
            ShiftTemplate.ShiftType prevType = prevOpt.get().getShiftType();
            // 如果前一天是休假，不做任何限制
            if (prevType != ShiftTemplate.ShiftType.off) {
                if (!canConnect(prevType, newType)) {
                    throw new IllegalArgumentException("班別銜接不合法");
                }
            }
        }

        // 2. **後一天**（這裡是重點！）
        Optional<EmployeeSchedule> nextOpt = scheduleRepo.findByEmployeeAndShiftDate(employee, newDate.plusDays(1))
                .stream().findFirst();
        if (nextOpt.isPresent()) {
            ShiftTemplate.ShiftType nextType = nextOpt.get().getShiftType();
            if (nextType != ShiftTemplate.ShiftType.off) {
                if (!canConnect(newType, nextType)) {
                    throw new IllegalArgumentException("班別銜接不合法");
                }
            }
        }
    }

    // 班別銜接規則
    private boolean canConnect(ShiftTemplate.ShiftType prev, ShiftTemplate.ShiftType next) {
        switch (prev) {
            case morning:
                return next == ShiftTemplate.ShiftType.morning ||
                        next == ShiftTemplate.ShiftType.evening ||
                        next == ShiftTemplate.ShiftType.night ||
                        next == ShiftTemplate.ShiftType.off;
            case evening:
                return next == ShiftTemplate.ShiftType.evening ||
                        next == ShiftTemplate.ShiftType.night ||
                        next == ShiftTemplate.ShiftType.off;
            case night:
                return next == ShiftTemplate.ShiftType.night ||
                        next == ShiftTemplate.ShiftType.off;
            case off:
                return true; // 休假後接什麼都行
            default:
                return true;
        }
    }



    @Override
    public EmployeeSchedule getSchedule(Long id) {
        return scheduleRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("找不到此排班紀錄"));
    }

    @Override
    public List<EmployeeSchedule> getAllSchedules() {
        return scheduleRepo.findAll();
    }

    @Override
    @Transactional
    public EmployeeSchedule updateSchedule(Long id, ScheduleRequest request) {
        EmployeeSchedule existing = getSchedule(id);
        User employee = userRepo.findById(request.getEmployeeId()).orElseThrow(() -> new IllegalArgumentException("找不到員工"));

        // 檢查 role 是否為員工
        if (employee.getRole().getId() != 2) {
            throw new IllegalArgumentException("僅限員工（staff）可以被排班");
        }

        LocalDate newDate = request.getShiftDate();
        ShiftTemplate.ShiftType newType = request.getShiftType();

        // 如果有改 shiftDate 或 shiftType，就做重複排班檢查
        if (!existing.getShiftDate().equals(newDate) || !existing.getShiftType().equals(newType)) {
            scheduleRepo.findByEmployeeAndShiftDateAndShiftType(employee, newDate, newType)
                    .ifPresent(conflict -> {
                        throw new IllegalArgumentException("此員工在該日已排此班別，請勿重複排班");
                    });
        }

        // 取得新班別的模板
        ShiftTemplate newTemplate = shiftTemplateRepo.findByShiftType(newType)
                .orElseThrow(() -> new IllegalArgumentException("查無班別定義"));

        // 驗證：休息時間 + 連續工作天數
        validateConsecutiveDays(employee, request.getShiftDate(), request.getShiftType());
        validateShiftSequence(employee, request.getShiftDate(), request.getShiftType());
        // 更新欄位
        existing.setEmployee(employee);
        existing.setShiftDate(newDate);
        existing.setShiftType(newType);
        existing.setNote(request.getNote());
        existing.setStatus(request.getStatus() != null ? request.getStatus() : existing.getStatus());

        return scheduleRepo.save(existing);
    }

    @Override
    @Transactional
    public void cancelSchedule(Long id) {
        EmployeeSchedule schedule = getSchedule(id);
        scheduleRepo.delete(schedule);
    }

    @Override
    public List<ScheduleCalendarView> getSchedulesByMonth(int year, int month) {
        LocalDate start = DateUtils.getMonthStartDate(year, month);
        LocalDate end = DateUtils.getMonthEndDate(year, month);

        List<EmployeeSchedule> schedules = scheduleRepo.findByShiftDateBetween(start, end);

        return schedules.stream().map(s -> new ScheduleCalendarView(
                s.getId(),
                s.getEmployee().getId(),
                s.getEmployee().getFullName(),
                s.getShiftDate(),
                s.getShiftType(),
                s.getNote(),
                s.getStatus()
        )).toList();
    }

    @Override
    public List<EmployeeSchedule> getEmployeeSchedulesByMonth(Long employeeId, int year, int month) {
        User user = userRepo.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("找不到員工"));
        if (user.getRole().getId() != 2) {
            throw new IllegalArgumentException("此使用者不是員工");
        }

        LocalDate start = DateUtils.getMonthStartDate(year, month);
        LocalDate end = DateUtils.getMonthEndDate(year, month);

        return scheduleRepo.findByEmployeeAndShiftDateBetween(user, start, end);
    }

    @Override
    public List<ScheduleStatusSummary> getScheduleSummaryByMonth(int year, int month) {
        LocalDate start = DateUtils.getMonthStartDate(year, month);
        LocalDate end = DateUtils.getMonthEndDate(year, month);

        List<LocalDate> allDays = start.datesUntil(end.plusDays(1)).toList();

        // 建立 LocalDate → 總數 Map
        Map<LocalDate, Long> countMap = scheduleRepo.findByShiftDateBetween(start, end).stream()
                .collect(Collectors.groupingBy(EmployeeSchedule::getShiftDate, Collectors.counting()));

        return allDays.stream()
                .map(date -> new ScheduleStatusSummary(date, countMap.getOrDefault(date, 0L)))
                .toList();
    }


}
