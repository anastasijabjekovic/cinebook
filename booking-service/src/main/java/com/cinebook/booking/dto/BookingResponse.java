package com.cinebook.booking.dto;

import com.cinebook.booking.entity.Booking;

import java.time.LocalDateTime;

public record BookingResponse(
        Long id,
        Long userId,
        Long movieId,
        Integer seats,
        LocalDateTime createdAt
) {
    public static BookingResponse from(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getUserId(),
                booking.getMovieId(),
                booking.getSeats(),
                booking.getCreatedAt()
        );
    }
}
