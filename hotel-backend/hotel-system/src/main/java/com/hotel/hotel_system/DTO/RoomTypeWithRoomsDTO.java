package com.hotel.hotel_system.DTO;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

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
