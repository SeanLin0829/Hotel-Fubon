package com.hotel.hotel_system.repository;

import com.hotel.hotel_system.entity.ShiftTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShiftTemplateRepository extends JpaRepository<ShiftTemplate, Long> {


    //根據班別類型查詢對應的班別定義（例如：查詢 morning 班的時間）。

    Optional<ShiftTemplate> findByShiftType(ShiftTemplate.ShiftType shiftType);
}
