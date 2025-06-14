package com.hotel.hotel_system.controller;

import com.hotel.hotel_system.DTO.ReservationCreateRequest;
import com.hotel.hotel_system.DTO.ReservationDTO;
import com.hotel.hotel_system.DTO.RoomSimpleDTO;
import com.hotel.hotel_system.DTO.RoomTypeWithRoomsDTO;
import com.hotel.hotel_system.entity.Reservation;
import com.hotel.hotel_system.repository.RoomTypeRepository;
import com.hotel.hotel_system.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;
    private final RoomTypeRepository roomTypeRepository;


    //依房型取得所有房間清單
    @GetMapping("/with-rooms")
    public ResponseEntity<List<RoomTypeWithRoomsDTO>> getAllRoomTypesWithRooms() {
        List<RoomTypeWithRoomsDTO> result = roomTypeRepository.findAll().stream()
                .map(type -> new RoomTypeWithRoomsDTO(
                        type.getId(),
                        type.getName(),
                        type.getBasePrice(),
                        type.getRooms().stream()
                                .map(room -> new RoomSimpleDTO(room.getId(), room.getRoomNumber()))
                                .toList()
                ))
                .toList();

        return ResponseEntity.ok(result);
    }


    //查詢所有訂房
    @GetMapping
    public ResponseEntity<List<ReservationDTO>> getAllReservations() {
        List<Reservation> reservations = reservationService.getAllReservations();
        List<ReservationDTO> dtoList = reservations.stream()
                .map(ReservationDTO::new)
                .toList();
        return ResponseEntity.ok(dtoList);
    }

    //  建立訂房
    @PostMapping
    public ResponseEntity<Reservation> createReservation(@RequestBody ReservationCreateRequest request) {
        Reservation reservation = reservationService.createReservation(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(reservation);
    }

    //  透過訂單ID查詢單一訂房資料
    @GetMapping("/{id}")
    public ResponseEntity<ReservationDTO> getReservationById(@PathVariable Long id) {
        Reservation reservation = reservationService.getReservationById(id);
        return ResponseEntity.ok(new ReservationDTO(reservation));
    }

    //  查詢某使用者所有訂單
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Reservation>> getReservationsByUserId(@PathVariable Long userId) {
        List<Reservation> reservations = reservationService.getReservationsByUserId(userId);
        return ResponseEntity.ok(reservations);
    }

    //  透過訂單ID修改訂單
    @PatchMapping("/{id}")
    public ResponseEntity<Reservation> updateReservation(
            @PathVariable Long id,
            @RequestBody ReservationCreateRequest request) {
        Reservation updated = reservationService.updateReservation(id, request);
        return ResponseEntity.ok(updated);
    }

    //  透過訂單ID取消訂單
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelReservation(@PathVariable Long id) {
        reservationService.cancelReservation(id);
        return ResponseEntity.noContent().build();
    }

    // 透過訂單ID更新訂單狀態(預約中, 已入住, 已退房, 已取消)
    @PatchMapping("/{id}/status")
    public ResponseEntity<Reservation> updateReservationStatus(
            @PathVariable Long id,
            @RequestParam Reservation.ReservationStatus status
    ) {
        Reservation updated = reservationService.updateReservationStatus(id, status);
        return ResponseEntity.ok(updated);
    }

}