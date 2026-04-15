package com.cinebook.notification.dto;

/**
 * Mirror of the event published by booking-service.
 * Fields must match the JSON produced by the publisher.
 */
public record BookingNotificationEvent(
        Long bookingId,
        Long userId,
        Long movieId,
        Integer seats
) {}
