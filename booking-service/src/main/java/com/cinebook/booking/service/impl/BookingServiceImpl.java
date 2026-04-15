package com.cinebook.booking.service.impl;

import com.cinebook.booking.config.RabbitMQConfig;
import com.cinebook.booking.dto.BookingNotificationEvent;
import com.cinebook.booking.dto.BookingResponse;
import com.cinebook.booking.dto.CreateBookingRequest;
import com.cinebook.booking.entity.Booking;
import com.cinebook.booking.repository.BookingRepository;
import com.cinebook.booking.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final RabbitTemplate    rabbitTemplate;

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getAllBookings() {
        log.debug("Fetching all bookings");
        return bookingRepository.findAll()
                .stream()
                .map(BookingResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public BookingResponse createBooking(CreateBookingRequest request) {
        log.debug("Creating booking: userId={}, movieId={}, seats={}",
                request.userId(), request.movieId(), request.seats());

        Booking booking = Booking.builder()
                .userId(request.userId())
                .movieId(request.movieId())
                .seats(request.seats())
                .build();

        Booking saved = bookingRepository.save(booking);
        log.info("Created booking id={}", saved.getId());

        publishNotification(saved);

        return BookingResponse.from(saved);
    }

    private void publishNotification(Booking booking) {
        BookingNotificationEvent event = new BookingNotificationEvent(
                booking.getId(),
                booking.getUserId(),
                booking.getMovieId(),
                booking.getSeats()
        );

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_CINEBOOK,
                RabbitMQConfig.ROUTING_KEY_BOOKING,
                event
        );

        log.info("Published booking notification event for bookingId={} to exchange={}",
                booking.getId(), RabbitMQConfig.EXCHANGE_CINEBOOK);
    }
}
