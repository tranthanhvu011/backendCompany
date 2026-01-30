package com.company.email_service;

import com.company.common.dto.event.EmailEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Kafka Consumer - Listens for email events and sends emails
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailEventListener {

    private final EmailSenderService emailSenderService;

    /**
     * Listen for email events from Kafka
     */
    @KafkaListener(topics = "email-topic", groupId = "email-service-group")
    public void handleEmailEvent(EmailEvent event) {
        log.info("Received email event: {} for {}", event.getEventType(), event.getTo());
        
        try {
            switch (event.getEventType()) {
                case "OTP_EMAIL" -> {
                    String otp = (String) event.getTemplateData();
                    emailSenderService.sendOtpEmail(event.getTo(), otp);
                }
                case "WELCOME_EMAIL" -> {
                    String username = (String) event.getTemplateData();
                    emailSenderService.sendWelcomeEmail(event.getTo(), username);
                }
                case "RESET_PASSWORD_EMAIL" -> {
                    String token = (String) event.getTemplateData();
                    emailSenderService.sendResetPasswordEmail(event.getTo(), token);
                }
                case "EMAIL_SEND" -> {
                    // Generic email with HTML content
                    if (event.getHtmlContent() != null) {
                        emailSenderService.sendHtmlEmail(
                            event.getTo(), 
                            event.getSubject(), 
                            event.getHtmlContent()
                        );
                    } else {
                        emailSenderService.sendSimpleEmail(
                            event.getTo(), 
                            event.getSubject(), 
                            event.getTemplateData().toString()
                        );
                    }
                }
                default -> log.warn("Unknown email event type: {}", event.getEventType());
            }
            
            log.info("Email sent successfully for event: {}", event.getEventId());
            
        } catch (Exception e) {
            log.error("Failed to send email for event: {}", event.getEventId(), e);
            // TODO: Implement retry logic or dead letter queue
        }
    }
}
