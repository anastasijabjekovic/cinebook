package com.cinebook.movie.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

public record CreateMovieRequest(

        @NotBlank(message = "Title is required")
        @Size(min = 1, max = 200, message = "Title must be between 1 and 200 characters")
        String title,

        @NotBlank(message = "Genre is required")
        @Size(max = 100, message = "Genre must not exceed 100 characters")
        String genre,

        @NotNull(message = "Duration is required")
        @Positive(message = "Duration must be a positive number of minutes")
        Integer duration,

        @NotNull(message = "Showtime is required")
        @Future(message = "Showtime must be a future date and time")
        LocalDateTime showtime
) {}
