package com.hotel.hotel_system.DTO;

import lombok.Data;

// 用於簡化房間資訊的資料傳輸物件，包含房間ID和房間號碼。
@Data
public class RoomSimpleDTO {
    private Long roomId;
    private String roomNumber;

    public RoomSimpleDTO(Long roomId, String roomNumber) {
        this.roomId = roomId;
        this.roomNumber = roomNumber;
    }
}
