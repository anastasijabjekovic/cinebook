package com.cinebook.movie.dto;

import com.cinebook.movie.entity.Movie;

import java.time.LocalDateTime;

public record MovieResponse(
        Long id,
        String title,
        String genre,
        Integer duration,
        LocalDateTime showtime,
        LocalDateTime createdAt
) {
    public static MovieResponse from(Movie movie) {
        return new MovieResponse(
                movie.getId(),
                movie.getTitle(),
                movie.getGenre(),
                movie.getDuration(),
                movie.getShowtime(),
                movie.getCreatedAt()
        );
    }
}
