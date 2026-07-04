package com.example.animalvoiceprint.controller;

import com.example.animalvoiceprint.dto.ApiResponse;
import com.example.animalvoiceprint.dto.UserCreateRequest;
import com.example.animalvoiceprint.dto.UserUpdateRequest;
import com.example.animalvoiceprint.entity.SysUser;
import com.example.animalvoiceprint.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('admin')")
public class UserController {
    
    private final UserService userService;
    
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<SysUser>>> getAllUsers() {
        List<SysUser> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success(users));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SysUser>> getUserById(@PathVariable("id") Integer userId) {
        SysUser user = userService.getUserById(userId);
        return ResponseEntity.ok(ApiResponse.success(user));
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<SysUser>> createUser(@Valid @RequestBody UserCreateRequest request) {
        SysUser user = userService.createUser(request);
        return ResponseEntity.ok(ApiResponse.success("用户创建成功", user));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SysUser>> updateUser(@PathVariable("id") Integer userId,
                                                           @RequestBody UserUpdateRequest request) {
        SysUser user = userService.updateUser(userId, request);
        return ResponseEntity.ok(ApiResponse.success("用户更新成功", user));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable("id") Integer userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok(ApiResponse.success("用户删除成功", null));
    }
    
    @GetMapping("/role/{role}")
    public ResponseEntity<ApiResponse<List<SysUser>>> getUsersByRole(@PathVariable("role") String role) {
        List<SysUser> users = userService.getUsersByRole(role);
        return ResponseEntity.ok(ApiResponse.success(users));
    }
}