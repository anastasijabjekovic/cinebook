package com.cinebook.booking.dto;

/**
 * Payload published to the {@code booking-notifications} RabbitMQ queue
 * after a successful booking is persisted.
 */
public record BookingNotificationEvent(
        Long bookingId,
        Long userId,
        Long movieId,
        Integer seats
) {}
