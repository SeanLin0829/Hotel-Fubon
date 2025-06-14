package com.hotel.hotel_system.service.serviceImpl;

import com.hotel.hotel_system.DTO.UserCreateRequest;
import com.hotel.hotel_system.entity.Role;
import com.hotel.hotel_system.entity.User;
import com.hotel.hotel_system.repository.RoleRepository;
import com.hotel.hotel_system.repository.UserRepository;
import com.hotel.hotel_system.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    public User login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("帳號不存在: " + username));
        if (!user.getPassword().equals(password)) {
            throw new IllegalArgumentException("密碼錯誤");
        }
        return user;
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("使用者 ID 不存在: " + id));
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("找不到帳號: " + username));
    }

    @Override
    public User createUser(UserCreateRequest req) {
        if (userRepository.findByUsername(req.getUsername()).isPresent()) {
            throw new IllegalArgumentException("帳號已存在: " + req.getUsername());
        }

        if (req.getRole() == null || req.getRole().isBlank()) {
            throw new IllegalArgumentException("請提供角色名稱");
        }

        Role role = roleRepository.findByName(req.getRole())
                .orElseThrow(() -> new EntityNotFoundException("角色不存在: " + req.getRole()));

        User user = new User(
                req.getUsername(),
                req.getFullName(),
                req.getEmail(),
                req.getPhone(),
                req.getPassword()
        );
        user.setRole(role);

        return userRepository.save(user);
    }

    @Override
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("使用者 ID 不存在: " + id);
        }
        userRepository.deleteById(id);
    }
}
