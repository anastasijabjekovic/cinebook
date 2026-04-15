package com.cinebook.movie.dto;

/**
 * Aggregated response combining movie details with the user who requested them.
 * Produced by {@code GET /movies/{id}/with-user?userId=X}.
 */
public record MovieWithUserResponse(
        MovieResponse movie,
        UserDto user
) {}
