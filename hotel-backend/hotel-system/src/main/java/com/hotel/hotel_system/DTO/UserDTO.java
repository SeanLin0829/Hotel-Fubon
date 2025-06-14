package com.hotel.hotel_system.DTO;

import lombok.*;

import java.util.List;


// 用於表示使用者資訊的資料傳輸物件，包含使用者ID、用戶名、全名、電子郵件、電話和角色等信息。
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
