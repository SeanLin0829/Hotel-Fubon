package com.hotel.hotel_system.DTO;

import lombok.Data;

//用於創建新使用者並指定角色的請求資料傳輸物件。
@Data
public class LoginRequest {
    private String username;
    private String password;
}
