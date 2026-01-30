package com.company.common.dto.event;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.Map;

/**
 * Push notification event.
 * Published when a service needs to send push notification.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PushNotificationEvent extends BaseEvent {

    private Long userId;
    private String title;
    private String body;
    private String imageUrl;
    private String deepLink;
    private Map<String, String> data;
    private boolean silent;

    /**
     * Create simple push event
     */
    public static PushNotificationEvent simple(Long userId, String title, String body) {
        PushNotificationEvent event = new PushNotificationEvent();
        event.setEventId(java.util.UUID.randomUUID().toString());
        event.setEventType("PUSH_SEND");
        event.setTimestamp(java.time.LocalDateTime.now());
        event.setUserId(userId);
        event.setTitle(title);
        event.setBody(body);
        return event;
    }
}
