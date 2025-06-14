package com.hotel.hotel_system.controller;

import com.hotel.hotel_system.DTO.RestaurantReservationCreateRequest;
import com.hotel.hotel_system.DTO.RestaurantReservationUpdateRequest;
import com.hotel.hotel_system.entity.RestaurantReservation;
import com.hotel.hotel_system.entity.RestaurantTable;
import com.hotel.hotel_system.service.RestaurantReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/restaurant")
@RequiredArgsConstructor
public class RestaurantReservationController {

    private final RestaurantReservationService restaurantReservationService;


    // 查詢所有訂位
    @GetMapping
    public ResponseEntity<List<RestaurantReservation>> getAllReservations() {
        return ResponseEntity.ok(restaurantReservationService.getAllReservations());
    }
    // 依日期查詢訂位
    @GetMapping("/by-date")
    public ResponseEntity<List<RestaurantReservation>> getReservationsByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(restaurantReservationService.getReservationsByDate(date.atStartOfDay()));
    }

    //  建立新訂位
    @PostMapping
    public ResponseEntity<RestaurantReservation> createReservation(
            @RequestBody RestaurantReservationCreateRequest request
    ) {
        RestaurantReservation reservation = restaurantReservationService.createReservation(
                request.getUserId(),
                request.getReservationTime(),
                request.getNumberOfGuests(),
                request.getTableIds(),
                request.getNote(),
                request.getGuestName(),
                request.getGuestPhone()
        );
        return ResponseEntity.ok(reservation);
    }

    // 取得單筆訂位
    @GetMapping("/{id}")
    public ResponseEntity<RestaurantReservation> getReservation(@PathVariable Long id) {
        Optional<RestaurantReservation> reservation = restaurantReservationService.getReservation(id);
        return reservation.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    //  查詢使用者所有訂位
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<RestaurantReservation>> getUserReservations(@PathVariable Long userId) {
        return ResponseEntity.ok(restaurantReservationService.getUserReservations(userId));
    }

    //  取消訂位
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelReservation(@PathVariable Long id) {
        restaurantReservationService.cancelReservation(id);
        return ResponseEntity.noContent().build();
    }

    //  查詢空桌（可預約桌位）
    @GetMapping("/available-tables")
    public ResponseEntity<List<RestaurantTable>> getAvailableTables(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end
    ) {
        List<RestaurantTable> availableTables = restaurantReservationService.getAvailableTables(start, end);
        return ResponseEntity.ok(availableTables);
    }

    //  修改訂位
    @PutMapping("/{id}")
    public ResponseEntity<RestaurantReservation> updateReservation(
            @PathVariable Long id,
            @RequestBody RestaurantReservationUpdateRequest request
    ) {
        RestaurantReservation updated = restaurantReservationService.updateReservation(
                id,
                request.getReservationTime(),
                request.getNumberOfGuests(),
                request.getTableIds(),
                request.getNote(),
                request.getGuestName(),
                request.getGuestPhone()
        );
        return ResponseEntity.ok(updated);
    }
}
