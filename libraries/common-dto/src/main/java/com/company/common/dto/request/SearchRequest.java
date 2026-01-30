package com.company.common.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Standard search request parameters.
 * Used for search endpoints with filtering and pagination.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchRequest {

    @Size(max = 255, message = "Search keyword too long")
    private String keyword;

    private String status;

    private String category;

    private String startDate;

    private String endDate;

    @Builder.Default
    private int page = 0;

    @Builder.Default
    private int size = 20;

    @Builder.Default
    private String sortBy = "createdAt";

    @Builder.Default
    private String sortDirection = "desc";

    /**
     * Check if keyword is present
     */
    public boolean hasKeyword() {
        return keyword != null && !keyword.trim().isEmpty();
    }

    /**
     * Check if date range filter is present
     */
    public boolean hasDateRange() {
        return startDate != null && endDate != null;
    }

    /**
     * Convert to PageRequest
     */
    public PageRequest toPageRequest() {
        return PageRequest.builder()
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();
    }
}
