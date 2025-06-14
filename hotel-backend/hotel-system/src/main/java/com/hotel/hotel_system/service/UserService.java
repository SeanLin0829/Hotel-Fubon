package com.hotel.hotel_system.service;

import com.hotel.hotel_system.entity.User;

import java.util.List;


public interface UserService {
    List<User> findAll();
    User findById(Long id);
    User findByUsername(String username);
    void deleteUser(Long id);
    User login(String username, String password);
}
