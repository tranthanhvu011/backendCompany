package com.company.common.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to require specific role for a controller method or class.
 * Usage: @RequireRole("SELLER") on controller class or method.
 * 
 * ADMIN role always has access (bypass).
 * Enforced by RequireRoleAspect via AOP.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {
    String value();
}
