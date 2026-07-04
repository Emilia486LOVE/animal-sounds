package com.example.animalvoiceprint.service;

import com.example.animalvoiceprint.dto.LoginRequest;
import com.example.animalvoiceprint.dto.LoginResponse;
import com.example.animalvoiceprint.dto.PasswordChangeRequest;
import com.example.animalvoiceprint.dto.UserCreateRequest;
import com.example.animalvoiceprint.entity.SysUser;
import com.example.animalvoiceprint.exception.BusinessException;
import com.example.animalvoiceprint.repository.SysUserRepository;
import com.example.animalvoiceprint.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final SysUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CaptchaService captchaService;
    
    public AuthService(AuthenticationManager authenticationManager, JwtTokenProvider tokenProvider,
                       SysUserRepository userRepository, PasswordEncoder passwordEncoder,
                       CaptchaService captchaService) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.captchaService = captchaService;
    }
    
    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = tokenProvider.generateToken((UserDetails) authentication.getPrincipal());
        
        SysUser user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        LoginResponse response = new LoginResponse();
        response.setUserId(user.getUserId());
        response.setUsername(user.getUsername());
        response.setRealName(user.getRealName());
        response.setRole(user.getRole());
        response.setToken(token);
        
        return response;
    }
    
    public SysUser register(UserCreateRequest request) {
        captchaService.validateCaptcha(request.getCaptchaId(), request.getCaptchaCode());
        
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("用户名已存在");
        }
        
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException("两次输入的密码不一致");
        }
        
        if (!isValidPassword(request.getPassword())) {
            throw new BusinessException("密码强度不足：密码长度至少6位，且必须包含字母和数字");
        }
        
        if (!isValidUsername(request.getUsername())) {
            throw new BusinessException("用户名格式错误：只能包含字母、数字和下划线");
        }
        
        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setRole(request.getRole());
        user.setStatus(1);
        
        return userRepository.save(user);
    }
    
    private boolean isValidPassword(String password) {
        if (password.length() < 6) {
            return false;
        }
        boolean hasLetter = false;
        boolean hasDigit = false;
        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) {
                hasLetter = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            }
        }
        return hasLetter && hasDigit;
    }
    
    private boolean isValidUsername(String username) {
        return username.matches("^[a-zA-Z0-9_]+$");
    }
    
    public void changePassword(PasswordChangeRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        SysUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new BusinessException("旧密码错误");
        }
        
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
    
    public SysUser getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在"));
    }
}