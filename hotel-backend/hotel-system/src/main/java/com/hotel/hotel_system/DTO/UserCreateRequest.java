package com.hotel.hotel_system.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserCreateRequest {
    private String username;
    private String fullName;
    private String email;
    private String phone;
    private String password;
    private String role; // 由前端提供角色名稱（可為 "customer"）
}
