package com.company.common.dto.event;

import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Email notification event.
 * Published when a service needs to send an email.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class EmailEvent extends BaseEvent {

    private String to;
    private String[] cc;
    private String[] bcc;
    private String subject;
    private String templateName;
    private Object templateData;
    private String htmlContent;
    private boolean highPriority;

    /**
     * Create simple email event
     */
    public static EmailEvent simple(String to, String subject, String templateName, Object data) {
        EmailEvent event = new EmailEvent();
        event.setEventId(java.util.UUID.randomUUID().toString());
        event.setEventType("EMAIL_SEND");
        event.setTimestamp(java.time.LocalDateTime.now());
        event.setTo(to);
        event.setSubject(subject);
        event.setTemplateName(templateName);
        event.setTemplateData(data);
        return event;
    }
}
