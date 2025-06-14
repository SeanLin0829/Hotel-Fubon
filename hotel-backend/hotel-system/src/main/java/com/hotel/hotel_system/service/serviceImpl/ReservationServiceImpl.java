package com.hotel.hotel_system.service.serviceImpl;

import com.hotel.hotel_system.DTO.ReservationCreateRequest;
import com.hotel.hotel_system.entity.Reservation;
import com.hotel.hotel_system.entity.ReservationRoom;
import com.hotel.hotel_system.entity.Room;
import com.hotel.hotel_system.entity.User;
import com.hotel.hotel_system.repository.ReservationRepository;
import com.hotel.hotel_system.repository.ReservationRoomRepository;
import com.hotel.hotel_system.repository.RoomRepository;
import com.hotel.hotel_system.repository.UserRepository;
import com.hotel.hotel_system.service.ReservationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationRoomRepository reservationRoomRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;

    @Override
    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    @Override
    @Transactional
    public Reservation createReservation(ReservationCreateRequest req) {
        User user = null;
        if (req.getUserId() != null) {
            user = userRepository.findById(req.getUserId())
                    .orElseThrow(() -> new EntityNotFoundException("找不到使用者"));
        }

        if (!req.getCheckoutDate().isAfter(req.getCheckinDate())) {
            throw new IllegalArgumentException("退房日期必須晚於入住日期至少一天");
        }

        List<Room> rooms = roomRepository.findAllById(req.getRoomIds());
        if (rooms.size() != req.getRoomIds().size()) {
            throw new IllegalArgumentException("部分房間不存在");
        }

        List<Long> overlappingRoomIds = reservationRoomRepository.findOverlappingRoomIds(
                req.getRoomIds(), req.getCheckinDate(), req.getCheckoutDate());

        if (!overlappingRoomIds.isEmpty()) {
            throw new IllegalStateException("以下房間在該時段已被預約: " + overlappingRoomIds);
        }

        long days = ChronoUnit.DAYS.between(req.getCheckinDate(), req.getCheckoutDate());
        BigDecimal totalPrice = rooms.stream()
                .map(room -> room.getType().getBasePrice().multiply(BigDecimal.valueOf(days)))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Reservation reservation = new Reservation();
        reservation.setUser(user); // 可能為 null
        reservation.setGuestName(req.getGuestName());
        reservation.setGuestPhone(req.getGuestPhone());
        reservation.setCheckinDate(req.getCheckinDate());
        reservation.setCheckoutDate(req.getCheckoutDate());
        reservation.setGuestCount(req.getGuestCount());
        reservation.setNote(req.getNote());
        reservation.setTotalPrice(totalPrice);
        reservation.setStatus(Reservation.ReservationStatus.預約中);

        Reservation saved = reservationRepository.save(reservation);

        for (Room room : rooms) {
            ReservationRoom rr = new ReservationRoom();
            rr.setReservation(saved);
            rr.setRoom(room);
            reservationRoomRepository.save(rr);
        }

        return saved;
    }

    @Override
    public Reservation getReservationById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("找不到訂單"));
    }

    @Override
    public List<Reservation> getReservationsByUserId(Long userId) {
        return reservationRepository.findByUserId(userId);
    }

    @Override
    @Transactional
    public Reservation updateReservation(Long id, ReservationCreateRequest req) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("找不到訂單"));


        //  允許修改已取消訂單，並自動恢復為預約中
        if (reservation.getStatus() == Reservation.ReservationStatus.已取消) {
            reservation.setStatus(Reservation.ReservationStatus.預約中);
        }

        // 取得新房間資料
        List<Room> newRooms = roomRepository.findAllById(req.getRoomIds());

        if (newRooms.size() != req.getRoomIds().size()) {
            throw new IllegalArgumentException("部分房間不存在");
        }

        // 檢查重疊（排除自己原本的房間）
        List<Long> overlapping = reservationRoomRepository.findOverlappingRoomIds(
                        req.getRoomIds(), req.getCheckinDate(), req.getCheckoutDate()
                ).stream()
                .filter(roomId -> reservation.getReservationRooms().stream()
                        .noneMatch(rr -> rr.getRoom().getId().equals(roomId)))
                .toList();

        if (!overlapping.isEmpty()) {
            throw new IllegalStateException("以下房間在該時段已被預約: " + overlapping);
        }

        // 🔥 刪除原本關聯的房間
        reservationRoomRepository.deleteAll(reservation.getReservationRooms());
        reservation.getReservationRooms().clear();

        // 更新訂房資訊
        reservation.setCheckinDate(req.getCheckinDate());
        reservation.setCheckoutDate(req.getCheckoutDate());
        reservation.setGuestCount(req.getGuestCount());
        reservation.setNote(req.getNote());

        long days = ChronoUnit.DAYS.between(req.getCheckinDate(), req.getCheckoutDate());
        BigDecimal totalPrice = newRooms.stream()
                .map(room -> room.getType().getBasePrice().multiply(BigDecimal.valueOf(days)))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        reservation.setTotalPrice(totalPrice);

        // 重新建立關聯
        for (Room room : newRooms) {
            ReservationRoom rr = new ReservationRoom();
            rr.setReservation(reservation);
            rr.setRoom(room);
            reservationRoomRepository.save(rr);
        }

        return reservationRepository.save(reservation);
    }


    @Override
    @Transactional
    public void cancelReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("找不到訂單"));

        reservation.setStatus(Reservation.ReservationStatus.已取消);
        reservationRepository.save(reservation);
    }

    @Override
    public Reservation updateReservationStatus(Long id, Reservation.ReservationStatus newStatus) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("找不到訂單 ID: " + id));
        reservation.setStatus(newStatus);
        reservation.setUpdatedAt(LocalDateTime.now());
        return reservationRepository.save(reservation);
    }

}
