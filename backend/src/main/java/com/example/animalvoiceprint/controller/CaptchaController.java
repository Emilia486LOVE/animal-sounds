package com.example.animalvoiceprint.controller;

import com.example.animalvoiceprint.dto.ApiResponse;
import com.example.animalvoiceprint.service.CaptchaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class CaptchaController {
    
    private final CaptchaService captchaService;
    
    public CaptchaController(CaptchaService captchaService) {
        this.captchaService = captchaService;
    }
    
    @GetMapping("/captcha")
    public ResponseEntity<ApiResponse<CaptchaService.CaptchaResponse>> getCaptcha() {
        CaptchaService.CaptchaResponse response = captchaService.generateCaptcha();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}