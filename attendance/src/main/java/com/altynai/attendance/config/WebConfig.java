package com.altynai.attendance.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final SessionAuthInterceptor sessionAuthInterceptor;
    private final String[] allowedOrigins;

    public WebConfig(
            SessionAuthInterceptor sessionAuthInterceptor,
            @Value("${app.cors.allowed-origins:http://localhost:8082,http://127.0.0.1:8082}") String allowedOriginsRaw
    ) {
        this.sessionAuthInterceptor = sessionAuthInterceptor;
        this.allowedOrigins = java.util.Arrays.stream(allowedOriginsRaw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOrigins(allowedOrigins)
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(sessionAuthInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/",
                        "/index",
                        "/login",
                        "/error",
                        "/favicon.ico",
                        "/api/auth/**",
                        "/api/users/reset",
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/webjars/**"
                );
    }
}
