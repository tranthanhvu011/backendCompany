package com.company.common.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Base event class for all domain events.
 * Used for Kafka message publishing.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class BaseEvent {

    /**
     * Unique event ID
     */
    private String eventId;

    /**
     * Event type/name (e.g., "USER_CREATED", "ORDER_COMPLETED")
     */
    private String eventType;

    /**
     * Aggregate ID (e.g., user ID, order ID)
     */
    private String aggregateId;

    /**
     * Aggregate type (e.g., "USER", "ORDER")
     */
    private String aggregateType;

    /**
     * Event timestamp
     */
    private LocalDateTime timestamp;

    /**
     * User who triggered the event
     */
    private String triggeredBy;

    /**
     * Event version for schema evolution
     */
    @Builder.Default
    private int version = 1;

    /**
     * Additional metadata
     */
    private Map<String, Object> metadata;

    /**
     * Create new event with generated ID and timestamp
     */
    public static BaseEvent create(String eventType, String aggregateId, String aggregateType) {
        return BaseEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(eventType)
                .aggregateId(aggregateId)
                .aggregateType(aggregateType)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
