package com.company.email_service;

import com.netflix.discovery.converters.Auto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

/**
 * Email Sender Service - Handles actual email sending via SMTP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailSenderService {
    private final JavaMailSender mailSender;

    /**
     * Gửi email đơn giản (plain text)
     */
    public void sendSimpleEmail(String to, String subject, String body) {
        log.info("Sending simple email to: {}", to);
        
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        
        mailSender.send(message);
        log.info("Email sent successfully to: {}", to);
    }

    /**
     * Gửi email HTML
     */
    public void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        log.info("Sending HTML email to: {}", to);
        
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true); // true = HTML
        
        mailSender.send(message);
        log.info("HTML email sent successfully to: {}", to);
    }

    /**
     * Gửi email OTP
     */
    public void sendOtpEmail(String to, String otp) {
        String subject = "Mã xác thực OTP - Company Microservices";
        String body = String.format("""
            Xin chào,
            
            Mã OTP của bạn là: %s
            
            Mã này sẽ hết hạn sau 5 phút.
            Vui lòng không chia sẻ mã này với bất kỳ ai.
            
            Trân trọng,
            Company Team
            """, otp);
        
        sendSimpleEmail(to, subject, body);
    }

    /**
     * Gửi email chào mừng
     */
    public void sendWelcomeEmail(String to, String username) {
        String subject = "Chào mừng bạn đến với Company!";
        String body = String.format("""
            Xin chào %s,
            
            Chúc mừng bạn đã đăng ký tài khoản thành công!
            
            Bạn có thể đăng nhập và bắt đầu sử dụng dịch vụ của chúng tôi.
            
            Trân trọng,
            Company Team
            """, username);
        
        sendSimpleEmail(to, subject, body);
    }

    /**
     * Gửi email reset password
     */
    public void sendResetPasswordEmail(String to, String linkResetToken) {
        String subject = "Yêu cầu đặt lại mật khẩu";
        String body = String.format("""
            Xin chào,
            
            Bạn đã yêu cầu đặt lại mật khẩu.
            
            Đường dẫn: %s
            
            Đường dẫn này sẽ hết hạn sau 15 phút.
            Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.
            
            Trân trọng,
            Company Team
            """, linkResetToken);
        
        sendSimpleEmail(to, subject, body);
    }
}
