package com.company.userservice.service;

import com.company.common.core.exception.BusinessException;
import com.company.common.core.exception.ErrorCode;
import com.company.common.core.util.StringUtils;
import com.company.common.dto.event.EmailEvent;
import com.company.userservice.dto.data.RegisterData;
import com.company.userservice.dto.request.RegisterRequest;
import com.company.userservice.dto.request.SendOtpRequest;
import com.company.userservice.dto.response.MessageResponse;
import com.company.userservice.entity.User;
import com.company.userservice.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.company.common.core.constant.RedisConstants;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {
    private final UserRepository userRepository;
    private final RedisTemplate<Object, Object> redisTemplate;
    private final PasswordEncoder passwordEncoder;
    private final ObjectCodec objectCodec;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

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
            String totalAttemptsKey = "total_attempts:" + email;               // Đếm tổng số lần sai (cả 9 lần)

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
                .roles(Set.of("ROLE_USER"))
                .build();

        userRepository.save(user);

        // 6. Dọn dẹp Redis
        redisTemplate.delete(otpKey);
        redisTemplate.delete(RedisConstants.OTP_ATTEMPTS_PREFIX + email);
        redisTemplate.delete("total_attempts:" + email);
        return MessageResponse.success("Đăng ký thành công!");
    }
    public MessageResponse sendOTP(SendOtpRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email already exists!");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("Username already exists!");
        }
        String otp = String.format("%06d", new SecureRandom().nextInt(999999));

        redisTemplate.opsForValue().set(RedisConstants.OTP_PREFIX + request.getEmail(), otp,RedisConstants.OTP_EXPIRE_MINUTES, TimeUnit.MINUTES);
        kafkaTemplate.send("email-topic", String.valueOf(EmailEvent.builder()
                .eventType("OTP_EMAIL")
                .to(request.getEmail())
                .templateData(otp)
                .build()));
        return MessageResponse.success("Mã OTP đã gửi đến: " + request.getEmail());
    }
}
