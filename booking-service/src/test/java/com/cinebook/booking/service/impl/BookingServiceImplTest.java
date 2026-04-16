package com.cinebook.booking.service.impl;

import com.cinebook.booking.config.RabbitMQConfig;
import com.cinebook.booking.dto.BookingNotificationEvent;
import com.cinebook.booking.dto.BookingResponse;
import com.cinebook.booking.dto.CreateBookingRequest;
import com.cinebook.booking.entity.Booking;
import com.cinebook.booking.repository.BookingRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookingServiceImpl — unit tests")
class BookingServiceImplTest {

    // ── Collaborators (mocked) ────────────────────────────────────────────────

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    // ── Subject under test ────────────────────────────────────────────────────

    @InjectMocks
    private BookingServiceImpl bookingService;

    // ── Captor for the published RabbitMQ event ───────────────────────────────

    @Captor
    private ArgumentCaptor<BookingNotificationEvent> eventCaptor;

    // ── getAllBookings ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("getAllBookings — returns all bookings mapped to response DTOs")
    void getAllBookings_returnsAllBookingsMappedToResponses() {
        // given
        List<Booking> bookings = List.of(
                buildBooking(1L, 10L, 20L, 2),
                buildBooking(2L, 11L, 21L, 4)
        );
        when(bookingRepository.findAll()).thenReturn(bookings);

        // when
        List<BookingResponse> responses = bookingService.getAllBookings();

        // then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).id()).isEqualTo(1L);
        assertThat(responses.get(0).userId()).isEqualTo(10L);
        assertThat(responses.get(0).seats()).isEqualTo(2);
        assertThat(responses.get(1).id()).isEqualTo(2L);
        assertThat(responses.get(1).seats()).isEqualTo(4);
        verify(bookingRepository).findAll();
    }

    // ── createBooking ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("createBooking — persists booking with correct fields and returns response")
    void createBooking_persistsBookingAndReturnsCorrectResponse() {
        // given
        CreateBookingRequest request = new CreateBookingRequest(5L, 7L, 3);
        Booking savedBooking = buildBooking(100L, 5L, 7L, 3);
        when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);

        // when
        BookingResponse response = bookingService.createBooking(request);

        // then — response reflects saved entity
        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.userId()).isEqualTo(5L);
        assertThat(response.movieId()).isEqualTo(7L);
        assertThat(response.seats()).isEqualTo(3);
        assertThat(response.createdAt()).isNotNull();

        // verify correct entity was passed to repository
        ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository).save(bookingCaptor.capture());
        Booking persisted = bookingCaptor.getValue();
        assertThat(persisted.getUserId()).isEqualTo(5L);
        assertThat(persisted.getMovieId()).isEqualTo(7L);
        assertThat(persisted.getSeats()).isEqualTo(3);
    }

    @Test
    @DisplayName("createBooking — publishes notification to correct RabbitMQ exchange and routing key")
    void createBooking_publishesNotificationToCorrectExchangeAndRoutingKey() {
        // given
        CreateBookingRequest request = new CreateBookingRequest(5L, 7L, 3);
        Booking savedBooking = buildBooking(100L, 5L, 7L, 3);
        when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);

        // when
        bookingService.createBooking(request);

        // then — exact exchange and routing key must match RabbitMQConfig constants
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE_CINEBOOK),
                eq(RabbitMQConfig.ROUTING_KEY_BOOKING),
                any(BookingNotificationEvent.class)
        );
    }

    @Test
    @DisplayName("createBooking — notification event payload contains bookingId, userId, movieId and seats")
    void createBooking_notificationEventContainsCorrectPayload() {
        // given
        Long userId  = 42L;
        Long movieId = 17L;
        int  seats   = 4;
        CreateBookingRequest request = new CreateBookingRequest(userId, movieId, seats);
        Booking savedBooking = buildBooking(200L, userId, movieId, seats);
        when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);

        // when
        bookingService.createBooking(request);

        // then — capture and assert every field of the published event
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE_CINEBOOK),
                eq(RabbitMQConfig.ROUTING_KEY_BOOKING),
                eventCaptor.capture()
        );

        BookingNotificationEvent event = eventCaptor.getValue();
        assertThat(event.bookingId()).isEqualTo(200L);
        assertThat(event.userId()).isEqualTo(userId);
        assertThat(event.movieId()).isEqualTo(movieId);
        assertThat(event.seats()).isEqualTo(seats);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static Booking buildBooking(Long id, Long userId, Long movieId, int seats) {
        return Booking.builder()
                .id(id)
                .userId(userId)
                .movieId(movieId)
                .seats(seats)
                .createdAt(LocalDateTime.of(2024, 3, 10, 14, 0))
                .build();
    }
}
