package com.company.common.security.util;

import com.company.common.security.model.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * Utility class for accessing current authenticated user.
 */
public final class SecurityUtils {

    private SecurityUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Get current authentication
     */
    public static Optional<Authentication> getCurrentAuthentication() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication());
    }

    /**
     * Get current authenticated user
     */
    public static Optional<UserPrincipal> getCurrentUser() {
        return getCurrentAuthentication()
                .map(Authentication::getPrincipal)
                .filter(principal -> principal instanceof UserPrincipal)
                .map(principal -> (UserPrincipal) principal);
    }

    /**
     * Get current user ID
     */
    public static Optional<Long> getCurrentUserId() {
        return getCurrentUser().map(UserPrincipal::getId);
    }

    /**
     * Get current user ID or throw exception
     */
    public static Long requireCurrentUserId() {
        return getCurrentUserId()
                .orElseThrow(() -> new SecurityException("User not authenticated"));
    }

    /**
     * Get current username
     */
    public static Optional<String> getCurrentUsername() {
        return getCurrentUser().map(UserPrincipal::getUsername);
    }

    /**
     * Check if user is authenticated
     */
    public static boolean isAuthenticated() {
        return getCurrentAuthentication()
                .map(Authentication::isAuthenticated)
                .orElse(false);
    }

    /**
     * Check if current user has role
     */
    public static boolean hasRole(String role) {
        return getCurrentUser()
                .map(user -> user.hasRole(role))
                .orElse(false);
    }

    /**
     * Check if current user is admin
     */
    public static boolean isAdmin() {
        return hasRole("ADMIN");
    }

    /**
     * Check if current user is the owner of a resource
     */
    public static boolean isOwner(Long resourceOwnerId) {
        return getCurrentUserId()
                .map(userId -> userId.equals(resourceOwnerId))
                .orElse(false);
    }

    /**
     * Check if current user is admin or owner
     */
    public static boolean isAdminOrOwner(Long resourceOwnerId) {
        return isAdmin() || isOwner(resourceOwnerId);
    }
}
