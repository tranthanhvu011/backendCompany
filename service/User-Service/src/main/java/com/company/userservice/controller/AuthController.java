package com.company.userservice.controller;

import com.company.common.dto.response.ApiResponse;
import com.company.userservice.dto.request.*;
import com.company.userservice.dto.response.AuthResponse;
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
        return ResponseEntity.ok(ApiResponse.success("Ma OTP da duoc gui den email"));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegisterRequest request) {
        authService.registerUser(request);
        return ResponseEntity.ok(ApiResponse.success("Dang ky thanh cong"));
    }

    @GetMapping("/check-email")
    public ResponseEntity<ApiResponse<Boolean>> checkEmail(@RequestParam String email) {
        boolean exists = authService.checkEmailExists(email);
        return ResponseEntity.ok(ApiResponse.success(exists, "Kiem tra thanh cong"));
    }

    @GetMapping("/check-username")
    public ResponseEntity<ApiResponse<Boolean>> checkUsername(@RequestParam String username) {
        boolean exists = authService.checkUsernameExists(username);
        return ResponseEntity.ok(ApiResponse.success(exists, "Kiem tra thanh cong"));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            @RequestParam(name = "portal", defaultValue = "user") String portal
    ) {
        AuthResponse response = authService.loginUser(request, portal);
        return ResponseEntity.ok(ApiResponse.success(response, "Dang nhap thanh cong"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@RequestParam String email) {
        authService.forgotPassword(email);
        return ResponseEntity.ok(ApiResponse.success("Truy cap email cua ban de lay lai mat khau"));
    }
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Lay lai mat khau thanh cong"));
    }
    @PostMapping("refresh-token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refeshToken(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Refresh token thanh cong"));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody LogoutRequest request) {
        authService.logoutUser(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success("Dang xuat thanh cong"));
    }
}
