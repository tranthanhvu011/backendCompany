package com.company.email_service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Email Controller - REST API for manual email testing
 */
@RestController
@RequestMapping("/api/v1/email")
@RequiredArgsConstructor
public class EmailController {

    private final EmailSenderService emailSenderService;

    /**
     * Health check
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "email-service"
        ));
    }

    /**
     * Test gửi email đơn giản
     * POST /api/email/test
     */
    @PostMapping("/test")
    public ResponseEntity<?> testEmail(@RequestBody TestEmailRequest request) {
        try {
            emailSenderService.sendSimpleEmail(
                request.to(),
                request.subject(),
                request.body()
            );
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Email sent to " + request.to()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    /**
     * Test gửi OTP email
     * POST /api/email/test-otp
     */
    @PostMapping("/test-otp")
    public ResponseEntity<?> testOtpEmail(@RequestParam String to) {
        try {
            String otp = String.format("%06d", new java.util.Random().nextInt(999999));
            emailSenderService.sendOtpEmail(to, otp);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "OTP email sent to " + to,
                "otp", otp // Chỉ để test, production không trả về OTP!
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    // DTO for test email request
    record TestEmailRequest(String to, String subject, String body) {}
}
