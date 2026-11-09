package com.SystemPetShop.shared.infrastructure.http;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CORSConfig implements WebMvcConfigurer {

    private final String allowedOrigin;
    private final boolean allowCredentials;

    public CORSConfig(@Value("${app.cors.allowed-origin}") String allowedOrigin,
                      @Value("${app.cors.allow-credentials}") boolean allowCredentials) {
        this.allowedOrigin = allowedOrigin;
        this.allowCredentials = allowCredentials;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(allowedOrigin)
                .allowedMethods(
                        "GET",
                        "POST",
                        "PUT",
                        "PATCH",
                        "DELETE",
                        "OPTIONS"
                )
                .allowedHeaders("*")
                .allowCredentials(allowCredentials);
    }
}
