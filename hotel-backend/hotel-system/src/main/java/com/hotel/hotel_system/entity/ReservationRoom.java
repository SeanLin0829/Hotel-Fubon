package com.hotel.hotel_system.entity;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "reservation_room")
@Data
public class ReservationRoom {

    @EmbeddedId
    private ReservationRoomId id = new ReservationRoomId();

    @ManyToOne
    @MapsId("reservationId")
    @JoinColumn(name = "reservation_id")
    @JsonIgnore
    private Reservation reservation;

    @ManyToOne
    @MapsId("roomId")
    @JoinColumn(name = "room_id")
    private Room room;
}

