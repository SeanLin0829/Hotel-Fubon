package com.hotel.hotel_system.service;

import com.hotel.hotel_system.DTO.ReservationCreateRequest;
import com.hotel.hotel_system.entity.Reservation;

import java.util.List;

public interface ReservationService {
    Reservation createReservation(ReservationCreateRequest request);
    Reservation getReservationById(Long id);
    List<Reservation> getReservationsByUserId(Long userId);
    Reservation updateReservation(Long id, ReservationCreateRequest request);
    void cancelReservation(Long id);
    List<Reservation> getAllReservations();
    Reservation updateReservationStatus(Long id, Reservation.ReservationStatus newStatus);
}
