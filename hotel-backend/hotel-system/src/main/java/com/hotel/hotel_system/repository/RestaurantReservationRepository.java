package com.hotel.hotel_system.repository;

import com.hotel.hotel_system.entity.RestaurantReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RestaurantReservationRepository extends JpaRepository<RestaurantReservation, Long> {


    // 根據日期查詢餐廳預約
    List<RestaurantReservation> findByReservationTimeBetween(LocalDateTime start, LocalDateTime end);

    // 找出在特定時間區間，某一張餐桌是否有被預約
    @Query("""
        SELECT r FROM RestaurantReservation r 
        JOIN r.tables t 
        WHERE t.id = :tableId
        AND r.status = 'BOOKED'
        AND (
            (r.reservationTime < :endTime AND r.endTime > :reservationTime)
        )
    """)
    List<RestaurantReservation> findConflictingReservations(
            @Param("tableId") Long tableId,
            @Param("reservationTime") LocalDateTime reservationTime,
            @Param("endTime") LocalDateTime endTime
    );

    // 查詢某位使用者在特定時間是否已有預約
    @Query("""
        SELECT r FROM RestaurantReservation r
        WHERE r.user.id = :userId
        AND r.status = 'BOOKED'
        AND r.reservationTime < :endTime
        AND r.endTime > :reservationTime
    """)
    List<RestaurantReservation> findUserConflicts(
            @Param("userId") Long userId,
            @Param("reservationTime") LocalDateTime reservationTime,
            @Param("endTime") LocalDateTime endTime
    );
}