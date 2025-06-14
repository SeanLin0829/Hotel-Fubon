package com.hotel.hotel_system.DTO;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

// 用於表示房型及其包含的房間資訊的資料傳輸物件，包含房型ID、名稱、基礎價格和房間列表。
@Data
public class RoomTypeWithRoomsDTO {
    private Long typeId;
    private String typeName;
    private BigDecimal basePrice;
    private List<RoomSimpleDTO> rooms;

    public RoomTypeWithRoomsDTO(Long typeId, String typeName, BigDecimal basePrice, List<RoomSimpleDTO> rooms) {
        this.typeId = typeId;
        this.typeName = typeName;
        this.basePrice = basePrice;
        this.rooms = rooms;
    }
}
