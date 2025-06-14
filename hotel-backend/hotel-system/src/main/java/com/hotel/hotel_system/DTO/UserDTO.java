package com.hotel.hotel_system.DTO;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String username;
    private String fullName;
    private String email;
    private String phone;
    private String role;
}
