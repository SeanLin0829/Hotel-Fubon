package com.hotel.hotel_system.controller;


import com.hotel.hotel_system.DTO.LoginRequest;
import com.hotel.hotel_system.DTO.LoginResponse;
import com.hotel.hotel_system.DTO.UserDTO;
import com.hotel.hotel_system.entity.User;
import com.hotel.hotel_system.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 使用者登入
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest req) {
        User user = userService.login(req.getUsername(), req.getPassword());
        LoginResponse res = new LoginResponse(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getRole().getName()
        );
        return ResponseEntity.ok(res);
    }

    // 查詢所有使用者
    @GetMapping
    public List<UserDTO> getAllUsers() {
        return userService.findAll().stream()
                .map(user -> new UserDTO(
                        user.getId(),
                        user.getUsername(),
                        user.getFullName(),
                        user.getEmail(),
                        user.getPhone(),
                        user.getRole().getName()
                ))
                .collect(Collectors.toList());
    }

    // 透過ID查詢單一使用者
    @GetMapping("/{id}")
    public UserDTO getUserById(@PathVariable Long id) {
        User user = userService.findById(id);
        return new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole().getName()
        );
    }

    // 查詢所有role為staff的使用者
    @GetMapping("/staff")
    public List<UserDTO> getAllStaff() {
        return userService.findAll().stream()
                .filter(user -> user.getRole() != null && "staff".equalsIgnoreCase(user.getRole().getName()))
                .map(user -> new UserDTO(
                        user.getId(),
                        user.getUsername(),
                        user.getFullName(),
                        user.getEmail(),
                        user.getPhone(),
                        user.getRole().getName()
                ))
                .collect(Collectors.toList());
    }

    // 查詢所有role為customer的使用者
    @GetMapping("/customer")
    public List<UserDTO> getAllCustomer() {
        return userService.findAll().stream()
                .filter(user -> user.getRole() != null && "customer".equalsIgnoreCase(user.getRole().getName()))
                .map(user -> new UserDTO(
                        user.getId(),
                        user.getUsername(),
                        user.getFullName(),
                        user.getEmail(),
                        user.getPhone(),
                        user.getRole().getName()
                ))
                .collect(Collectors.toList());
    }


    // 刪除使用者
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
