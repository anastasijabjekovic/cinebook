package com.cinebook.notification.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String QUEUE_BOOKING_NOTIFICATIONS = "booking-notifications";

    /**
     * Declare the queue so the service can start even when booking-service
     * hasn't published anything yet (idempotent if already declared by producer).
     */
    @Bean
    public Queue bookingNotificationsQueue() {
        return QueueBuilder.durable(QUEUE_BOOKING_NOTIFICATIONS).build();
    }

    /**
     * JSON converter configured with TypePrecendence.INFERRED so that
     * Spring AMQP resolves the target type from the @RabbitListener method
     * signature rather than from the __TypeId__ header (which would point
     * to the booking-service class name).
     */
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        ObjectMapper om = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(om);
        converter.setTypePrecendence(
                org.springframework.amqp.support.converter.AbstractJavaTypeMapper.TypePrecendence.INFERRED);
        return converter;
    }
}
