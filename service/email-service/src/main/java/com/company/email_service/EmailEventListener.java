package com.company.email_service;

import com.company.common.dto.event.EmailEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final ObjectMapper objectMapper;

    /**
     * Listen for email events from Kafka.
     * Message is received as JSON String (sent by AuthService via objectMapper.writeValueAsString).
     */
    @KafkaListener(topics = "email-topic", groupId = "email-service-group")
    public void handleEmailEvent(String message) {
        log.info("Received raw Kafka message: {}", message);

        try {
            EmailEvent event = objectMapper.readValue(message, EmailEvent.class);
            log.info("Parsed email event: {} for {}", event.getEventType(), event.getTo());

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
            log.error("Failed to process email event: {}", e.getMessage(), e);
        }
    }
}
