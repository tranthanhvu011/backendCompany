package com.company.userservice.controller;

import com.company.common.dto.response.ApiResponse;
import com.company.userservice.dto.request.LoginRequest;
import com.company.userservice.dto.request.RegisterRequest;
import com.company.userservice.dto.request.SendOtpRequest;
import com.company.userservice.dto.response.MessageResponse;
import com.company.userservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    @PostMapping("/send-otp")
    public ResponseEntity<ApiResponse<Void>> sendOtp(@Valid @RequestBody SendOtpRequest request) {
        authService.sendOTP(request);
        return ResponseEntity.ok(ApiResponse.success("Mã OTP đã được gửi đến email"));
    }
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegisterRequest request) {
        authService.registerUser(request);
        return ResponseEntity.ok(ApiResponse.success("Đăng ký thành công"));
    }
    @GetMapping("/check-email")
    public ResponseEntity<ApiResponse<Boolean>> checkEmail(@RequestParam String email) {
        boolean exists = authService.checkEmailExists(email);
        return ResponseEntity.ok(ApiResponse.success(exists, "Kiểm tra thành công"));
    }
    @GetMapping("/check-username")
    public ResponseEntity<ApiResponse<Boolean>> checkUsername(@RequestParam String username) {
        boolean exists = authService.checkUsernameExists(username);
        return ResponseEntity.ok(ApiResponse.success(exists, "Kiểm tra thành công"));
    }
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<MessageResponse>> login(@Valid @RequestBody LoginRequest request) {
        MessageResponse response = authService.loginUser(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Đăng nhập thành công"));
    }
}
