package com.smartpantry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Smart Pantry backend.
 *
 * When the frontend has been built via the "build-frontend" Maven profile,
 * the compiled React app is copied into src/main/resources/static and is
 * served automatically by Spring Boot's embedded servlet container, so the
 * whole product (API + UI) starts with a single `java -jar smart-pantry.jar`.
 */
@SpringBootApplication
public class SmartPantryApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartPantryApplication.class, args);
    }
}
