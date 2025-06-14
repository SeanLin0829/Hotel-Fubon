package com.hotel.hotel_system.DTO;

import lombok.Data;

@Data
public class RoomSimpleDTO {
    private Long roomId;
    private String roomNumber;

    public RoomSimpleDTO(Long roomId, String roomNumber) {
        this.roomId = roomId;
        this.roomNumber = roomNumber;
    }
}
