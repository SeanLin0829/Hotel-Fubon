package com.hotel.hotel_system.repository;

import com.hotel.hotel_system.entity.ReservationRoom;
import com.hotel.hotel_system.entity.ReservationRoomId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReservationRoomRepository extends JpaRepository<ReservationRoom, ReservationRoomId> {
    List<ReservationRoom> findByRoomId(Long roomId);
    List<ReservationRoom> findByReservationId(Long reservationId);

    @Query("""
    SELECT rr.room.id FROM ReservationRoom rr
    WHERE rr.reservation.status <> '已取消'
    AND rr.reservation.checkinDate < :checkoutDate
    AND rr.reservation.checkoutDate > :checkinDate
    """)
    List<Long> findRoomIdsWithOverlappingReservations(
            @Param("checkinDate") LocalDate checkinDate,
            @Param("checkoutDate") LocalDate checkoutDate);

    @Query("""
SELECT rr.room.id FROM ReservationRoom rr
WHERE rr.room.id IN :roomIds
AND rr.reservation.status <> '已取消'
AND rr.reservation.checkinDate < :checkoutDate
AND rr.reservation.checkoutDate > :checkinDate
""")
    List<Long> findOverlappingRoomIds(
            @Param("roomIds") List<Long> roomIds,
            @Param("checkinDate") LocalDate checkinDate,
            @Param("checkoutDate") LocalDate checkoutDate);
}
