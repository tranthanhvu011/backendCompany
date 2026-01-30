package com.company.userservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {
    private Boolean success;
    private String message;
    private Object data;
    public static MessageResponse success(String message) {
        return MessageResponse.builder()
                .success(true)
                .message(message)
                .build();
    }

    public static MessageResponse success(String message, Object data) {
        return MessageResponse.builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    public static MessageResponse error(String message) {
        return MessageResponse.builder()
                .success(false)
                .message(message)
                .build();
    }
}
