package com.example.animalvoiceprint.controller;

import com.example.animalvoiceprint.dto.ApiResponse;
import com.example.animalvoiceprint.dto.LoginRequest;
import com.example.animalvoiceprint.dto.LoginResponse;
import com.example.animalvoiceprint.dto.PasswordChangeRequest;
import com.example.animalvoiceprint.dto.UserCreateRequest;
import com.example.animalvoiceprint.entity.SysUser;
import com.example.animalvoiceprint.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private final AuthService authService;
    
    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("登录成功", response));
    }
    
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<SysUser>> register(@Valid @RequestBody UserCreateRequest request) {
        SysUser user = authService.register(request);
        return ResponseEntity.ok(ApiResponse.success("注册成功", user));
    }
    
    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody PasswordChangeRequest request) {
        authService.changePassword(request);
        return ResponseEntity.ok(ApiResponse.success("密码修改成功", null));
    }
    
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<SysUser>> getCurrentUser() {
        SysUser user = authService.getCurrentUser();
        return ResponseEntity.ok(ApiResponse.success(user));
    }
}