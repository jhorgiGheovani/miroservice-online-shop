package com.jhorgi.payment_service.config;

import com.jhorgi.payment_service.security.ApiKeyFilter;
import com.jhorgi.payment_service.security.JwtAuthFilter;
import com.jhorgi.payment_service.security.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecurityConfig {

    @Value("${app.internal-api-key}")
    private String internalApiKey;

    @Bean
    public FilterRegistrationBean<ApiKeyFilter> apiKeyFilterRegistration() {
        FilterRegistrationBean<ApiKeyFilter> registration = new FilterRegistrationBean<>(new ApiKeyFilter(internalApiKey));
        registration.addUrlPatterns("/payments");
        registration.setOrder(1);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<JwtAuthFilter> jwtFilterRegistration(JwtUtil jwtUtil) {
        FilterRegistrationBean<JwtAuthFilter> registration = new FilterRegistrationBean<>(new JwtAuthFilter(jwtUtil));
        registration.addUrlPatterns("/payments/*/pay", "/payments/*/fail", "/payments/*");
        registration.setOrder(2);
        return registration;
    }
}
