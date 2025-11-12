package com.chada.conf;

import org.springframework.context.annotation.Configuration;

@Configuration
public class CorsConfig {
    // CORS is now handled by CorsFilter to avoid duplicate headers
    // This configuration is disabled to prevent conflicts
    // If you need to use WebMvcConfigurer instead, remove CorsFilter
}