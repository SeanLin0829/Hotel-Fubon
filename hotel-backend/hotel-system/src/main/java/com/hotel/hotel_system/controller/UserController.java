package com.hotel.hotel_system.controller;


import com.hotel.hotel_system.DTO.LoginRequest;
import com.hotel.hotel_system.DTO.LoginResponse;
import com.hotel.hotel_system.DTO.UserCreateRequest;
import com.hotel.hotel_system.DTO.UserDTO;
import com.hotel.hotel_system.entity.User;
import com.hotel.hotel_system.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

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


    @PostMapping
    public ResponseEntity<UserDTO> createUser(@RequestBody UserCreateRequest req) {
        User created = userService.createUser(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new UserDTO(
                        created.getId(),
                        created.getUsername(),
                        created.getFullName(),
                        created.getEmail(),
                        created.getPhone(),
                        created.getRole().getName()
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
