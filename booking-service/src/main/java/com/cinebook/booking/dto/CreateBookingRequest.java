package com.cinebook.booking.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateBookingRequest(

        @NotNull(message = "userId is required")
        Long userId,

        @NotNull(message = "movieId is required")
        Long movieId,

        @NotNull(message = "seats is required")
        @Positive(message = "seats must be a positive number")
        @Min(value = 1, message = "At least one seat must be booked")
        Integer seats
) {}
