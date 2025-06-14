package com.hotel.hotel_system.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

// 用於用戶登錄後返回的資料傳輸物件，包含用戶ID、用戶名、全名和角色。
@Data
@AllArgsConstructor
public class LoginResponse {
    private Long id;
    private String username;
    private String fullName;
    private String role;
}
