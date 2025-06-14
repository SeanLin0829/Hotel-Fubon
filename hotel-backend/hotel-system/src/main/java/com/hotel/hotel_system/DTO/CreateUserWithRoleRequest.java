package com.hotel.hotel_system.DTO;

import lombok.Data;

@Data
public class CreateUserWithRoleRequest {
    private String username;
    private String password;
    private String fullName;
    private String email;
    private String phone;
    private String role;
}
