package com.company.common.dto.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * Simplified user info for displaying in other contexts.
 * Use this when you need minimal user data (e.g., in order, comment, review).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserSummaryDto {

    private Long id;
    private String username;
    private String fullName;
    private String avatar;

    /**
     * Create from full UserDto
     */
    public static UserSummaryDto from(UserDto userDto) {
        if (userDto == null) return null;
        return UserSummaryDto.builder()
                .id(userDto.getId())
                .username(userDto.getUsername())
                .fullName(userDto.getFullName())
                .avatar(userDto.getAvatar())
                .build();
    }
}
