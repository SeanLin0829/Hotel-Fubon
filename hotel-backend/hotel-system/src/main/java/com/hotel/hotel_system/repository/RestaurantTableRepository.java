package com.hotel.hotel_system.repository;

import com.hotel.hotel_system.entity.RestaurantTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RestaurantTableRepository extends JpaRepository<RestaurantTable, Long> {

    // 查詢特定人數以上的可用桌
    List<RestaurantTable> findByCapacityGreaterThanEqualAndStatus(int capacity, RestaurantTable.TableStatus status);


    // 取得所有指定 ID 的餐桌
    List<RestaurantTable> findByIdIn(List<Long> ids);


    // 查詢在指定時間範圍內可用的餐桌
    @Query("""
    SELECT t FROM RestaurantTable t
    WHERE t.status = 'AVAILABLE'
    AND t.id NOT IN (
        SELECT t2.id FROM RestaurantReservation r
        JOIN r.tables t2
        WHERE r.status = 'BOOKED'
        AND r.reservationTime < :endTime
        AND r.endTime > :startTime
    )
""")
    List<RestaurantTable> findAvailableTables(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );
}
