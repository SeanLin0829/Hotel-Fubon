package com.hotel.hotel_system.service.serviceImpl;

import com.hotel.hotel_system.entity.Role;
import com.hotel.hotel_system.entity.User;
import com.hotel.hotel_system.repository.RoleRepository;
import com.hotel.hotel_system.repository.UserRepository;
import com.hotel.hotel_system.service.UserRoleService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserRoleServiceImpl implements UserRoleService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    public User createUserWithRole(User user, String roleName) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new IllegalArgumentException("帳號已存在: " + user.getUsername());
        }

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new EntityNotFoundException("角色不存在: " + roleName));

        user.setRole(role); // ✅ 改這裡
        return userRepository.save(user);
    }

    @Override
    public void assignRoleToUser(Long userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("使用者不存在: " + userId));

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new EntityNotFoundException("角色不存在: " + roleName));

        user.setRole(role); // ✅ 改這裡
        userRepository.save(user);
    }
}