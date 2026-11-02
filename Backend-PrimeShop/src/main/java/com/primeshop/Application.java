package com.primeshop;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
@EntityScan(basePackages = "com.primeshop")
public class Application {

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        dotenv.entries().forEach(entry -> {
            String key = entry.getKey();
            String value = entry.getValue();
            System.setProperty(key, value);

            // Ánh xạ key dạng UPPER_UNDERSCORE sang dot.notation cho Spring System Property
            if ("SPRING_PROFILES_ACTIVE".equalsIgnoreCase(key)) {
                System.setProperty("spring.profiles.active", value);
            }
            if ("DB_URL".equalsIgnoreCase(key)) {
                System.setProperty("spring.datasource.url", value);
            }
            if ("DB_USERNAME".equalsIgnoreCase(key)) {
                System.setProperty("spring.datasource.username", value);
            }
            if ("DB_PASSWORD".equalsIgnoreCase(key)) {
                System.setProperty("spring.datasource.password", value);
            }
        });

        SpringApplication.run(Application.class, args);
    }
}