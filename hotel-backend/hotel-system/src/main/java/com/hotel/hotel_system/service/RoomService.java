package com.hotel.hotel_system.service;

import com.hotel.hotel_system.entity.Room;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public interface RoomService {
    List<Room> findAvailableRooms(LocalDate checkin, LocalDate checkout);
}
