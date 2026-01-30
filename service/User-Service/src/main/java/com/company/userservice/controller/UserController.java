package com.company.userservice.controller;

import com.company.common.dto.response.ApiResponse;
import com.company.common.dto.user.UserDto;
import com.company.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ApiResponse<List<UserDto>> getAllUsers() {
        return ApiResponse.success(userService.findAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<UserDto> getUser(@PathVariable Long id) {
        return ApiResponse.success(userService.findById(id));
    }
}
