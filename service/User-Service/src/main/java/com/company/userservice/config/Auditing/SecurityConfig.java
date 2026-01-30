package com.company.userservice.config.Auditing;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private static final String[] PUBLIC_ENDPOINTS = {
            "/api/auth/**", "/actuator/**","/v3/api-docs/**", "/swagger-ui/**",
    };
    @Bean
    public SecurityFilterChain springSecurityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable()).sessionManagement(
                session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        ).authorizeHttpRequests(auth -> auth.requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                .anyRequest().authenticated());
        return http.build();
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    

}
