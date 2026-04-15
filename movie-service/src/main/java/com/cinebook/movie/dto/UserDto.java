package com.cinebook.movie.dto;

import java.time.LocalDateTime;

/**
 * Mirror of user-service's UserResponse.
 * Fields must match the JSON produced by GET /users/{id}.
 */
public record UserDto(
        Long id,
        String name,
        String email,
        LocalDateTime createdAt
) {}
