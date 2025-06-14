package com.hotel.hotel_system.service.serviceImpl;

import com.hotel.hotel_system.entity.Room;
import com.hotel.hotel_system.repository.ReservationRoomRepository;
import com.hotel.hotel_system.repository.RoomRepository;
import com.hotel.hotel_system.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final ReservationRoomRepository reservationRoomRepository;

    // 查詢指定時段的空房
    @Override
    public List<Room> findAvailableRooms(LocalDate checkin, LocalDate checkout) {
        List<Long> bookedRoomIds = reservationRoomRepository
                .findRoomIdsWithOverlappingReservations(checkin, checkout);

        if (bookedRoomIds.isEmpty()) {
            return roomRepository.findAll();
        }

        return roomRepository.findAllByIdNotIn(bookedRoomIds);
    }
}
