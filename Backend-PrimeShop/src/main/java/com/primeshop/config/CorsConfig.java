package com.primeshop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        // THAY ĐỔI QUAN TRỌNG Ở ĐÂY:
                        // Dùng allowedOriginPatterns thay vì allowedOrigins 
                        // để chúng ta có thể dùng ký tự đại diện (*)
                        .allowedOriginPatterns(
                            "http://localhost:5173",      // Cho phép Dev Local
                            "https://*.vercel.app"        // [PRO TIP]: Cho phép tất cả các domain đuôi .vercel.app
                        )
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }
}