package com.hotel.hotel_system.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "shift_template")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShiftTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "shift_type", unique = true, nullable = false)
    private ShiftType shiftType;

    @Column(name = "start_time", nullable = false)
    private java.time.LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private java.time.LocalTime endTime;

    @Column(name = "is_cross_day", nullable = false)
    private boolean isCrossDay;

    public enum ShiftType {
        morning, evening, night, off
    }
}
