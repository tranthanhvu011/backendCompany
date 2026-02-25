package com.company.userservice.service;

import com.company.common.core.exception.BusinessException;
import com.company.common.core.exception.ErrorCode;
import com.company.common.core.util.StringUtils;
import com.company.common.dto.event.EmailEvent;
import com.company.common.security.jwt.JwtTokenProvider;
import com.company.userservice.dto.request.*;
import com.company.userservice.dto.response.AuthResponse;
import com.company.userservice.dto.response.MessageResponse;
import com.company.userservice.entity.User;
import com.company.userservice.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.company.common.core.constant.RedisConstants;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AuthService {
    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    public boolean checkEmailExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }
    public boolean checkUsernameExists(String username) {
        return userRepository.findByUsername(username).isPresent();
    }
    @Transactional
    public MessageResponse registerUser(RegisterRequest request) {
        String email = request.getEmail();

        // 1. Kiểm tra xem có đang bị block không
        String blockKey = RedisConstants.OTP_BLOCK_PREFIX + email;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(blockKey))) {
            throw new BusinessException(ErrorCode.OTP_BLOCKED);
        }

        // 2. Lấy OTP từ Redis
        String otpKey = RedisConstants.OTP_PREFIX + email;
        String storedOtp = (String) redisTemplate.opsForValue().get(otpKey);

        if (storedOtp == null) {
            throw new BusinessException(ErrorCode.OTP_EXPIRED);
        }

        // 3. Kiểm tra OTP
        if (!storedOtp.equals(request.getOtp())) {
            String attemptsKey = RedisConstants.OTP_ATTEMPTS_PREFIX + email;  // Đếm lần sai của OTP hiện tại
            String totalAttemptsKey = RedisConstants.TOTAL_ATTEMPTS_PREFIX + email;  // Đếm tổng số lần sai (cả 9 lần)

            Long currentAttempts = redisTemplate.opsForValue().increment(attemptsKey);
            Long totalAttempts = redisTemplate.opsForValue().increment(totalAttemptsKey);

            // Set expire cho totalAttempts (ví dụ 30 phút)
            redisTemplate.expire(totalAttemptsKey, 30, TimeUnit.MINUTES);

            // Nếu sai 9 lần tổng cộng -> BLOCK
            if (totalAttempts != null && totalAttempts >= 9) {
                redisTemplate.opsForValue().set(blockKey, "blocked", 15, TimeUnit.MINUTES);
                redisTemplate.delete(otpKey);
                redisTemplate.delete(attemptsKey);
                redisTemplate.delete(totalAttemptsKey);
                throw new BusinessException(ErrorCode.OTP_BLOCKED);
            }
            if (currentAttempts != null && currentAttempts >= RedisConstants.MAX_OTP_ATTEMPTS) {
                redisTemplate.delete(otpKey);
                redisTemplate.delete(attemptsKey);
                throw new BusinessException(ErrorCode.OTP_MAX_ATTEMPTS); // "Bạn đã nhập sai 3 lần, vui lòng gửi lại OTP"
            }
            throw new BusinessException(ErrorCode.OTP_INVALID);
        }

        // 4. OTP đúng -> Kiểm tra lại email/username
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }

        // 5. Tạo User và lưu vào DB
        User user = User.builder()
                .username(request.getUsername())
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .emailVerified(true)
                .enabled(true)
                .roles(Set.of("USER"))
                .build();

        userRepository.save(user);

        // 6. Dọn dẹp Redis
        redisTemplate.delete(otpKey);
        redisTemplate.delete(RedisConstants.OTP_ATTEMPTS_PREFIX + email);
        redisTemplate.delete(RedisConstants.TOTAL_ATTEMPTS_PREFIX + email);
        return MessageResponse.success("Đăng ký thành công!");
    }
    public MessageResponse sendOTP(SendOtpRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email already exists!");
        }
//        if (userRepository.existsByUsername(request.getUsername())) {
//            throw new BusinessException("Username already exists!");
//        }
        String otp = String.format("%06d", new SecureRandom().nextInt(999999));
        redisTemplate.opsForValue().set(RedisConstants.OTP_PREFIX + request.getEmail(), otp,RedisConstants.OTP_EXPIRE_MINUTES, TimeUnit.MINUTES);
        try {
            String eventJson = objectMapper.writeValueAsString(EmailEvent.builder()
                    .eventId(java.util.UUID.randomUUID().toString())
                    .eventType("OTP_EMAIL_REGISTERED")
                    .timestamp(java.time.LocalDateTime.now())
                    .to(request.getEmail())
                    .templateData(otp)
                    .build());
            kafkaTemplate.send("email-topic", eventJson);
        } catch (JsonProcessingException e) {
            throw new BusinessException("Failed to serialize email event");
        }
        return MessageResponse.success("Mã OTP đã gửi đến: " + request.getEmail());
    }
    @Transactional
    public AuthResponse loginUser(LoginRequest request, String portal) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        if (!user.isEnabled()) {
            throw new BusinessException(ErrorCode.ACCOUNT_DISABLED);
        }

        Set<String> roles = user.getRoles() != null ? user.getRoles() : Set.of("USER");
        enforcePortalPolicy(roles, portal);

        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getUsername(), roles);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getUsername());
        redisTemplate.opsForValue().set(RedisConstants.REFRESH_TOKEN_PREFIX + user.getEmail(), refreshToken, RedisConstants.REFRESH_EXPIRE_DAYS, TimeUnit.DAYS);
        user.setLastLoginAt(java.time.LocalDateTime.now());
        userRepository.save(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(String.valueOf(user.getId()))
                .email(user.getEmail())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roles(roles)
                .avatar(user.getAvatar())
                .build();
    }

    private void enforcePortalPolicy(Set<String> roles, String portal) {
        String normalizedPortal = portal == null ? "user" : portal.trim().toLowerCase(Locale.ROOT);
        boolean isAdmin = roles.contains("ADMIN");
        boolean isSeller = roles.contains("SELLER");

        switch (normalizedPortal) {
            case "admin" -> {
                if (!isAdmin) {
                    throw new BusinessException("Tài khoản này không có quyền truy cập", HttpStatus.FORBIDDEN);
                }
            }
            case "seller" -> {
                if (!isSeller && !isAdmin) {
                    throw new BusinessException("Tài khoản này không có quyền truy cập", HttpStatus.FORBIDDEN);
                }
            }
            case "user" -> {
                if (isAdmin) {
                    throw new BusinessException("Tài khoản này không có quyền truy cập", HttpStatus.FORBIDDEN);
                }
            }
            default -> log.warn("Unknown login portal '{}', skip portal restriction", portal);
        }
    }
    @Transactional
    public MessageResponse forgotPassword(String email) {
        if (!userRepository.existsByEmail(email)) {
            throw new BusinessException("Email no exists!");
        }
        String emailKey = RedisConstants.RESETPASSWORD_EMAIL_PREFIX + email;
        String oldToken = (String) redisTemplate.opsForValue().get(emailKey);
        if (oldToken != null) {
            redisTemplate.delete(RedisConstants.RESETPASSWORD_PREFIX + oldToken);
        }
        String resetToken = java.util.UUID.randomUUID().toString();
        String linkResetPassword = frontendUrl + "/reset-password?token=" + resetToken;
        redisTemplate.opsForValue().set(RedisConstants.RESETPASSWORD_PREFIX + resetToken, email, RedisConstants.RESET_PASSWORD_EXPIRE_MINUTES, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set(emailKey, resetToken, RedisConstants.RESET_PASSWORD_EXPIRE_MINUTES, TimeUnit.MINUTES);
        try {
            String eventJson = objectMapper.writeValueAsString(EmailEvent.builder()
                    .eventId(java.util.UUID.randomUUID().toString())
                    .eventType("RESET_PASSWORD_EMAIL")
                    .timestamp(java.time.LocalDateTime.now())
                    .to(email)
                    .templateData(linkResetPassword)
                    .build());
            kafkaTemplate.send("email-topic", eventJson);
        }catch (JsonProcessingException e) {
            throw new BusinessException("Failed to serialize email event");
        }
        return MessageResponse.success("Truy cập vào email của bạn để lấy lại mật khẩu");
    }
    @Transactional
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException("Mật khẩu xác nhận không khớp!");
        }
        String key = RedisConstants.RESETPASSWORD_PREFIX + request.getToken();
        String email = (String) redisTemplate.opsForValue().get(key);
        if (email == null) {
            throw new BusinessException("Link đã hết hạn hoặc không hợp lệ");
        }
        User user = userRepository.findByEmail(email).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        redisTemplate.delete(key);
        return MessageResponse.success("Lấy lại mật khẩu thành công");
    }
    public AuthResponse refeshToken(RefreshTokenRequest request) {
        if (!jwtTokenProvider.validateToken(request.getRefreshToken())) {
            throw new BusinessException("Invalid refresh token!");
        }
        Long userId = jwtTokenProvider.extractUserId(request.getRefreshToken());
        String username =  jwtTokenProvider.extractUsername(request.getRefreshToken());

        User user = userRepository.findByUsername(username).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        String storedToken = (String) redisTemplate.opsForValue().get(RedisConstants.REFRESH_TOKEN_PREFIX + user.getEmail());
        if (storedToken == null || !storedToken.equals(request.getRefreshToken())) {
            log.warn("Refresh token mismatch for user: {} - session expired or kicked", username);
            throw new BusinessException("Phiên đăng nhập đã hết hạn!");
        }
        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getUsername(), user.getRoles());
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getUsername());
        redisTemplate.opsForValue().set(RedisConstants.REFRESH_TOKEN_PREFIX + user.getEmail(), newRefreshToken, RedisConstants.REFRESH_EXPIRE_DAYS, TimeUnit.DAYS);
        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .userId(String.valueOf(user.getId()))
                .email(user.getEmail())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roles(user.getRoles() != null ? user.getRoles() : Set.of("USER"))
                .avatar(user.getAvatar())
                .build();
    }

    @Transactional
    public void logoutUser(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID);
        }
        String username = jwtTokenProvider.extractUsername(refreshToken);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        String storedToken = (String) redisTemplate.opsForValue().get(RedisConstants.REFRESH_TOKEN_PREFIX + user.getEmail());
        if (storedToken == null || !storedToken.equals(refreshToken)) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID);
        }

        redisTemplate.delete(RedisConstants.REFRESH_TOKEN_PREFIX + user.getEmail());
        log.info("User {} logged out successfully", username);
    }

    @Transactional
    public MessageResponse changePassword(Long userId, ChangePasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException("Mật khẩu xác nhận không khớp!");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.WRONG_PASSWORD);
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("User {} changed password successfully", user.getUsername());
        return MessageResponse.success("Đổi mật khẩu thành công!");
    }
}
