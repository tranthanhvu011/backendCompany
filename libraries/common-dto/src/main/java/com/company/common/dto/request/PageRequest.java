package com.company.common.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Standard pagination request parameters.
 * Used in controller methods for paginated endpoints.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageRequest {

    @Min(value = 0, message = "Page number cannot be negative")
    @Builder.Default
    private int page = 0;

    @Min(value = 1, message = "Page size must be at least 1")
    @Max(value = 100, message = "Page size cannot exceed 100")
    @Builder.Default
    private int size = 20;

    @Builder.Default
    private String sortBy = "createdAt";

    @Builder.Default
    private String sortDirection = "desc";

    /**
     * Convert to Spring Data Pageable
     */
    public org.springframework.data.domain.PageRequest toPageable() {
        org.springframework.data.domain.Sort sort = 
            "desc".equalsIgnoreCase(sortDirection)
                ? org.springframework.data.domain.Sort.by(sortBy).descending()
                : org.springframework.data.domain.Sort.by(sortBy).ascending();
        
        return org.springframework.data.domain.PageRequest.of(page, size, sort);
    }

    /**
     * Create default page request
     */
    public static PageRequest of(int page, int size) {
        return PageRequest.builder()
                .page(page)
                .size(size)
                .build();
    }

    /**
     * Create page request with sorting
     */
    public static PageRequest of(int page, int size, String sortBy, String sortDirection) {
        return PageRequest.builder()
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();
    }
}
