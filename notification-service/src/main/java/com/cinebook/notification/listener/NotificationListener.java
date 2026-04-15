package com.cinebook.notification.listener;

import com.cinebook.notification.dto.BookingNotificationEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificationListener {

    /**
     * Listens to the {@code booking-notifications} queue.
     * Spring AMQP deserializes the JSON message into {@link BookingNotificationEvent}
     * using the {@code Jackson2JsonMessageConverter} configured in {@code RabbitMQConfig}.
     */
    @RabbitListener(queues = "${cinebook.rabbitmq.queue.booking-notifications:booking-notifications}")
    public void handleBookingNotification(BookingNotificationEvent event) {
        log.info("[NOTIFICATION] Received booking event — bookingId={}, userId={}, movieId={}, seats={}",
                event.bookingId(),
                event.userId(),
                event.movieId(),
                event.seats());
    }
}
