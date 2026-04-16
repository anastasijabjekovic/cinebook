package com.cinebook.notification.listener;

import com.cinebook.notification.dto.BookingNotificationEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationListener — unit tests")
class NotificationListenerTest {

    // ── Subject under test ────────────────────────────────────────────────────
    // NotificationListener has no injected collaborators (it only uses SLF4J),
    // so @InjectMocks simply instantiates it via its no-arg constructor.

    @InjectMocks
    private NotificationListener notificationListener;

    // ── handleBookingNotification ─────────────────────────────────────────────

    @Test
    @DisplayName("handleBookingNotification — with valid event — processes without throwing")
    void handleBookingNotification_withValidEvent_doesNotThrow() {
        // given
        BookingNotificationEvent event = new BookingNotificationEvent(1L, 10L, 20L, 2);

        // when / then — listener must not propagate any exception
        // (uncaught exceptions in @RabbitListener cause message requeue/DLQ)
        assertThatCode(() -> notificationListener.handleBookingNotification(event))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("handleBookingNotification — with single seat booking — processes successfully")
    void handleBookingNotification_withSingleSeat_processesSuccessfully() {
        // given — edge case: minimum seats value
        BookingNotificationEvent event = new BookingNotificationEvent(99L, 1L, 5L, 1);

        // when / then
        assertThatCode(() -> notificationListener.handleBookingNotification(event))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("handleBookingNotification — with large seat count — processes successfully")
    void handleBookingNotification_withLargeSeatCount_processesSuccessfully() {
        // given — edge case: large group booking
        BookingNotificationEvent event = new BookingNotificationEvent(500L, 200L, 300L, 50);

        // when / then
        assertThatCode(() -> notificationListener.handleBookingNotification(event))
                .doesNotThrowAnyException();
    }
}
