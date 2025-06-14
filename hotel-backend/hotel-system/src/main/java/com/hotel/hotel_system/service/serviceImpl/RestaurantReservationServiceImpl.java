package com.hotel.hotel_system.service.serviceImpl;

import com.hotel.hotel_system.entity.RestaurantReservation;
import com.hotel.hotel_system.entity.RestaurantTable;
import com.hotel.hotel_system.entity.User;
import com.hotel.hotel_system.repository.RestaurantReservationRepository;
import com.hotel.hotel_system.repository.RestaurantTableRepository;
import com.hotel.hotel_system.repository.UserRepository;
import com.hotel.hotel_system.service.RestaurantReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RestaurantReservationServiceImpl implements RestaurantReservationService {

    private final RestaurantReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final RestaurantTableRepository tableRepository;

    // 查詢所有訂位
    @Override
    public List<RestaurantReservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    public List<RestaurantReservation> getReservationsByDate(LocalDateTime date) {
        // 取得當天的起始與結束時間
        LocalDateTime start = date.toLocalDate().atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        return reservationRepository.findByReservationTimeBetween(start, end);
    }

    // 建立餐廳訂位
    @Override
    public RestaurantReservation createReservation(
            Long userId,
            LocalDateTime reservationTime,
            int numberOfGuests,
            List<Long> tableIds,
            String note,
            String guestName,
            String guestPhone
    ) {
        // 自動結束時間為 +90 分鐘
        LocalDateTime endTime = reservationTime.plusMinutes(90);

        // 1. 判斷是否為會員預約
        User user = null;
        if (userId != null) {
            user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("使用者不存在"));
        }

        // 2. 檢查使用者是否已有重疊預約（匿名不檢查）
        if (user != null) {
            List<RestaurantReservation> userConflicts =
                    reservationRepository.findUserConflicts(userId, reservationTime, endTime);
            if (!userConflicts.isEmpty()) {
                throw new IllegalStateException("該時段您已有訂位，請勿重複預約。");
            }
        }

        // 3. 查詢餐桌
        List<RestaurantTable> tables = tableRepository.findByIdIn(tableIds);
        if (tables.size() != tableIds.size()) {
            throw new IllegalArgumentException("部分餐桌不存在");
        }

        for (RestaurantTable table : tables) {
            if (table.getStatus() != RestaurantTable.TableStatus.AVAILABLE) {
                throw new IllegalStateException("餐桌 " + table.getName() + " 不可預約");
            }

            List<RestaurantReservation> tableConflicts =
                    reservationRepository.findConflictingReservations(table.getId(), reservationTime, endTime);

            if (!tableConflicts.isEmpty()) {
                throw new IllegalStateException("餐桌 " + table.getName() + " 在該時段已被預約");
            }
        }

        // 4. 建立預約資料
        RestaurantReservation reservation = new RestaurantReservation();
        reservation.setUser(user); // 若是匿名預約則為 null
        reservation.setGuestName(guestName);
        reservation.setGuestPhone(guestPhone);
        reservation.setReservationTime(reservationTime);
        reservation.setEndTime(endTime);
        reservation.setNumberOfGuests(numberOfGuests);
        reservation.setNote(note);
        reservation.setStatus(RestaurantReservation.ReservationStatus.BOOKED);
        reservation.setCreatedAt(Timestamp.from(Instant.now()));
        reservation.setTables(tables);

        return reservationRepository.save(reservation);
    }

    // 查詢單一訂位
    @Override
    public Optional<RestaurantReservation> getReservation(Long id) {
        return reservationRepository.findById(id);
    }

    // 查詢某位使用者的所有訂位
    @Override
    public List<RestaurantReservation> getUserReservations(Long userId) {
        // 查詢某位使用者的所有訂位（可優化為 repository 查詢）
        return reservationRepository.findAll().stream()
                .filter(r -> r.getUser().getId().equals(userId))
                .collect(Collectors.toList());
    }

    // 取消餐廳訂位 (更改狀態為 CANCELLED)
    @Override
    public void cancelReservation(Long id) {
        // 取消指定 ID 的訂位
        RestaurantReservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("找不到該訂位"));
        reservation.setStatus(RestaurantReservation.ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);
    }

    // 查詢可用餐桌
    @Override
    public List<RestaurantTable> getAvailableTables(LocalDateTime startTime, LocalDateTime endTime) {
        return tableRepository.findAvailableTables(startTime, endTime);
    }

    // 更新餐廳訂位
    @Override
    public RestaurantReservation updateReservation(
            Long reservationId,
            LocalDateTime newTime,
            int newGuestCount,
            List<Long> newTableIds,
            String newNote,
            String guestName,
            String guestPhone
    ) {
        // 1. 取得原始預約資料
        RestaurantReservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("訂位不存在"));

        // 2. 檢查是否已取消（不能編輯）
        if (reservation.getStatus() == RestaurantReservation.ReservationStatus.CANCELLED) {
            throw new IllegalStateException("訂位已取消，無法修改");
        }

        LocalDateTime newEndTime = newTime.plusMinutes(90);

        // 3. 若為會員，執行避免重複預約檢查（匿名預約不檢查）
        User user = reservation.getUser();
        if (user != null) {
            Long userId = user.getId();
            List<RestaurantReservation> userConflicts = reservationRepository.findUserConflicts(
                    userId, newTime, newEndTime
            ).stream().filter(r -> !r.getId().equals(reservationId)).toList();

            if (!userConflicts.isEmpty()) {
                throw new IllegalStateException("該時段您已有其他訂位");
            }
        }

        // 4. 檢查新指定的餐桌狀態與是否時間衝突
        List<RestaurantTable> newTables = tableRepository.findByIdIn(newTableIds);
        if (newTables.size() != newTableIds.size()) {
            throw new IllegalArgumentException("部分餐桌不存在");
        }

        for (RestaurantTable table : newTables) {
            if (table.getStatus() != RestaurantTable.TableStatus.AVAILABLE) {
                throw new IllegalStateException("餐桌 " + table.getName() + " 不可預約");
            }

            List<RestaurantReservation> tableConflicts = reservationRepository
                    .findConflictingReservations(table.getId(), newTime, newEndTime)
                    .stream().filter(r -> !r.getId().equals(reservationId)).toList();

            if (!tableConflicts.isEmpty()) {
                throw new IllegalStateException("餐桌 " + table.getName() + " 在該時段已被預約");
            }
        }

        // 5. 更新資料（含匿名聯絡資訊）
        reservation.setReservationTime(newTime);
        reservation.setEndTime(newEndTime);
        reservation.setNumberOfGuests(newGuestCount);
        reservation.setNote(newNote);
        reservation.setTables(newTables);
        reservation.setGuestName(guestName);
        reservation.setGuestPhone(guestPhone);

        return reservationRepository.save(reservation);
    }
}
