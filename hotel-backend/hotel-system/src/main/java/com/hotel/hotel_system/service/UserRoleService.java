package com.hotel.hotel_system.service;

import com.hotel.hotel_system.entity.User;

public interface UserRoleService {
    User createUserWithRole(User user, String roleName);
    void assignRoleToUser(Long userId, String roleName);
}
