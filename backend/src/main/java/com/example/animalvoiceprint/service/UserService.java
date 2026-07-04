package com.example.animalvoiceprint.service;

import com.example.animalvoiceprint.dto.UserCreateRequest;
import com.example.animalvoiceprint.dto.UserUpdateRequest;
import com.example.animalvoiceprint.entity.SysUser;
import com.example.animalvoiceprint.exception.BusinessException;
import com.example.animalvoiceprint.exception.ResourceNotFoundException;
import com.example.animalvoiceprint.repository.SysUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    
    private final SysUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    public UserService(SysUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    public List<SysUser> getAllUsers() {
        return userRepository.findAll();
    }
    
    public SysUser getUserById(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在: " + userId));
    }
    
    public SysUser createUser(UserCreateRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("用户名已存在");
        }
        
        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setRole(request.getRole());
        user.setStatus(1);
        
        return userRepository.save(user);
    }
    
    public SysUser updateUser(Integer userId, UserUpdateRequest request) {
        SysUser user = getUserById(userId);
        
        if (request.getRealName() != null) {
            user.setRealName(request.getRealName());
        }
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }
        
        return userRepository.save(user);
    }
    
    public void deleteUser(Integer userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("用户不存在: " + userId);
        }
        userRepository.deleteById(userId);
    }
    
    public List<SysUser> getUsersByRole(String role) {
        return userRepository.findByRole(role);
    }
}