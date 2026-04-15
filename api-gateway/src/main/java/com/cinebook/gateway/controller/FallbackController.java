package com.cinebook.gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Handles circuit-breaker fallback responses when a downstream service
 * is unavailable. Each route in application.yml points its {@code fallbackUri}
 * to one of these endpoints.
 */
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/user-service")
    public ResponseEntity<Map<String, String>> userServiceFallback() {
        return fallback("user-service");
    }

    @GetMapping("/movie-service")
    public ResponseEntity<Map<String, String>> movieServiceFallback() {
        return fallback("movie-service");
    }

    @GetMapping("/booking-service")
    public ResponseEntity<Map<String, String>> bookingServiceFallback() {
        return fallback("booking-service");
    }

    @GetMapping("/notification-service")
    public ResponseEntity<Map<String, String>> notificationServiceFallback() {
        return fallback("notification-service");
    }

    private ResponseEntity<Map<String, String>> fallback(String service) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                "status", "SERVICE_UNAVAILABLE",
                "message", service + " is temporarily unavailable. Please try again later."
        ));
    }
}
