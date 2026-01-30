package com.company.common.dto.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * User DTO for transferring user data between services.
 * This is a shared representation of user data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDto {

    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String avatar;
    private boolean enabled;
    private boolean emailVerified;
    private Set<String> roles;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;

    /**
     * Get full name
     */
    public String getFullName() {
        if (firstName == null && lastName == null) {
            return null;
        }
        return String.format("%s %s", 
            firstName != null ? firstName : "", 
            lastName != null ? lastName : "").trim();
    }
}
