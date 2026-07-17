package com.example.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
        Jwt jwt,
        Cors cors
) {
    public record Jwt(
            String secret,
            long expirationMs,
            long resetExpirationMs,
            boolean invalidateOnStartup
    ) {
    }

    public record Cors(
            List<String> allowedOrigins
    ) {
    }
}
