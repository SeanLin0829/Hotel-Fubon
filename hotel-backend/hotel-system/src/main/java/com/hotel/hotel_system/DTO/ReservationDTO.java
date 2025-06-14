package com.hotel.hotel_system.DTO;

import com.hotel.hotel_system.entity.Reservation;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class ReservationDTO {
    private Long id;
    private String userName;
    private Long userId;
    private LocalDate checkinDate;
    private LocalDate checkoutDate;
    private int guestCount;
    private String status;
    private String note;
    private BigDecimal totalPrice;
    private List<String> roomNames;
    private List<Long> roomIds;
    private List<String> roomNumbers;
    private String guestName;
    private String guestPhone;

    public ReservationDTO(Reservation reservation) {
        this.id = reservation.getId();
        this.userName = reservation.getUser() != null
                ? reservation.getUser().getFullName()
                : reservation.getGuestName();
        this.checkinDate = reservation.getCheckinDate();
        this.checkoutDate = reservation.getCheckoutDate();
        this.guestCount = reservation.getGuestCount();
        this.status = reservation.getStatus().name();
        this.note = reservation.getNote();
        this.totalPrice = reservation.getTotalPrice();
        this.roomNames = reservation.getReservationRooms().stream()
                .map(rr -> rr.getRoom().getType().getName())
                .toList();
        this.userId = reservation.getUser() != null ? reservation.getUser().getId() : null; // 取得使用者 ID
        this.roomIds = reservation.getReservationRooms().stream()
                .map(rr -> rr.getRoom().getId())
                .collect(Collectors.toList()); // 取得所有房間 ID
        this.roomNumbers = reservation.getReservationRooms().stream()
                .map(rr -> rr.getRoom().getRoomNumber())
                .collect(Collectors.toList()); // 取得所有房間號碼
        this.userName = reservation.getUser() != null
                ? reservation.getUser().getFullName()
                : reservation.getGuestName(); // 非會員顯示 guestName
        this.userId = reservation.getUser() != null ? reservation.getUser().getId() : null;
        this.guestName = reservation.getGuestName();
        this.guestPhone = reservation.getGuestPhone();
    }
}

