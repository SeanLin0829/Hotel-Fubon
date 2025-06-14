package com.hotel.hotel_system.service;

import com.hotel.hotel_system.entity.RestaurantReservation;
import com.hotel.hotel_system.entity.RestaurantTable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public interface RestaurantReservationService {

    RestaurantReservation createReservation(
            Long userId,
            LocalDateTime reservationTime,
            int numberOfGuests,
            List<Long> tableIds,
            String note,
            String guestName,
            String guestPhone
    );

    Optional<RestaurantReservation> getReservation(Long id);

    List<RestaurantReservation> getUserReservations(Long userId);

    void cancelReservation(Long id);

    List<RestaurantTable> getAvailableTables(LocalDateTime startTime, LocalDateTime endTime);

    public RestaurantReservation updateReservation(
            Long reservationId,
            LocalDateTime newTime,
            int newGuestCount,
            List<Long> newTableIds,
            String newNote,
            String guestName,
            String guestPhone
    );

    public List<RestaurantReservation> getAllReservations();

    List<RestaurantReservation> getReservationsByDate(LocalDateTime date);

    RestaurantReservation updateReservationStatus(Long id, RestaurantReservation.ReservationStatus status);
}
