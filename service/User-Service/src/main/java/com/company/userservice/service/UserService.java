package com.company.userservice.service;

import com.company.common.core.constant.RedisConstants;
import com.company.common.core.exception.BusinessException;
import com.company.common.core.exception.ResourceNotFoundException;
import com.company.common.dto.user.UserDto;
import com.company.userservice.entity.User;
import com.company.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RestTemplate restTemplate;

    @Value("${app.chat-service-url:http://CHAT-SERVICE}")
    private String chatServiceUrl;

    public List<UserDto> findAll() {
        return userRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public UserDto findById(Long id) {
        return userRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }

    @Transactional
    public void addRoles(Long userId, Set<String> roles) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        if (user.getRoles() == null) {
            user.setRoles(new HashSet<>());
        }
        user.getRoles().addAll(roles);
        userRepository.save(user);
    }

    @Transactional
    public void removeRoles(Long userId, Set<String> roles) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        if (user.getRoles() != null) {
            user.getRoles().removeAll(roles);
            userRepository.save(user);
        }
    }

    @Transactional
    public void updateStatus(Long userId, boolean enabled) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        user.setEnabled(enabled);
        userRepository.save(user);

        if (!enabled) {
            // ── Force Logout: 3 steps ──
            // 1. Delete refresh token → user can't refresh session
            redisTemplate.delete(RedisConstants.REFRESH_TOKEN_PREFIX + user.getEmail());
            log.info("Deleted refresh token for disabled user: {} ({})", user.getUsername(), userId);

            // 2. Blacklist userId → JwtAuthenticationFilter will reject all API calls
            redisTemplate.opsForValue().set(
                    RedisConstants.USER_BLACKLIST_PREFIX + userId,
                    "disabled",
                    RedisConstants.USER_BLACKLIST_EXPIRE_HOURS, TimeUnit.HOURS
            );
            log.info("Blacklisted user: {} ({})", user.getUsername(), userId);

            // 3. Push WebSocket notification → instant logout on frontend
            pushForceLogoutNotification(userId);
        } else {
            // Re-enable: remove from blacklist
            redisTemplate.delete(RedisConstants.USER_BLACKLIST_PREFIX + userId);
            log.info("Removed blacklist for re-enabled user: {} ({})", user.getUsername(), userId);
        }
    }

    /**
     * Push ACCOUNT_DISABLED notification to user via Chat-Service's internal API.
     * Best-effort: if Chat-Service is down, user will still be blocked by Redis blacklist.
     */
    private void pushForceLogoutNotification(Long userId) {
        try {
            String url = chatServiceUrl + "/api/v1/internal/notifications/push";
            Map<String, Object> body = Map.of(
                    "userId", userId,
                    "type", "ACCOUNT_DISABLED"
            );
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
            log.info("Pushed ACCOUNT_DISABLED WS notification to user: {}", userId);
        } catch (Exception e) {
            // Best-effort: Redis blacklist is the primary security mechanism
            log.warn("Failed to push WS notification to user {}: {}", userId, e.getMessage());
        }
    }

    @Transactional
    public UserDto updateProfile(Long userId, com.company.userservice.dto.request.UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());
        if (request.getPhone() != null) user.setPhone(request.getPhone());

        User saved = userRepository.save(user);
        return toDto(saved);
    }

    @Transactional
    public UserDto updateAvatar(Long userId, MultipartFile file) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        try {
            String uploadDir = "uploads/avatars";
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : ".jpg";
            String newFilename = UUID.randomUUID().toString() + extension;

            Path filePath = uploadPath.resolve(newFilename);
            Files.copy(file.getInputStream(), filePath);

            String avatarUrl = "/uploads/avatars/" + newFilename;
            user.setAvatar(avatarUrl);
            User saved = userRepository.save(user);
            return toDto(saved);
        } catch (IOException e) {
            throw new BusinessException("Không thể tải lên ảnh đại diện");
        }
    }

    private UserDto toDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                .enabled(user.isEnabled())
                .emailVerified(user.isEmailVerified())
                .roles(user.getRoles())
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }
}

