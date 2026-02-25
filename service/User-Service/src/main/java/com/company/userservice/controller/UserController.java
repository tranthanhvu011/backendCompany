package com.company.userservice.controller;

import com.company.common.dto.response.ApiResponse;
import com.company.common.dto.user.UserDto;
import com.company.userservice.dto.request.ChangePasswordRequest;
import com.company.userservice.dto.request.UpdateProfileRequest;
import com.company.userservice.service.AuthService;
import com.company.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    @GetMapping
    public ApiResponse<List<UserDto>> getAllUsers() {
        return ApiResponse.success(userService.findAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<UserDto> getUser(@PathVariable Long id) {
        return ApiResponse.success(userService.findById(id));
    }

    // ===== Profile endpoints (authenticated user) =====

    @GetMapping("/me")
    public ApiResponse<UserDto> getMyProfile(@RequestAttribute("userId") Long userId) {
        return ApiResponse.success(userService.findById(userId));
    }

    @PutMapping("/me")
    public ApiResponse<UserDto> updateMyProfile(
            @RequestAttribute("userId") Long userId,
            @RequestBody UpdateProfileRequest request) {
        return ApiResponse.success(userService.updateProfile(userId, request));
    }

    @PutMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Đổi mật khẩu thành công!"));
    }

    @PostMapping("/me/avatar")
    public ApiResponse<UserDto> uploadAvatar(
            @RequestAttribute("userId") Long userId,
            @RequestParam("file") MultipartFile file) {
        return ApiResponse.success(userService.updateAvatar(userId, file));
    }
}
