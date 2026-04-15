package com.cinebook.booking.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String QUEUE_BOOKING_NOTIFICATIONS = "booking-notifications";
    public static final String EXCHANGE_CINEBOOK            = "cinebook.direct";
    public static final String ROUTING_KEY_BOOKING          = "booking.notification";

    @Bean
    public Queue bookingNotificationsQueue() {
        // durable = true: queue survives broker restart
        return QueueBuilder.durable(QUEUE_BOOKING_NOTIFICATIONS).build();
    }

    @Bean
    public DirectExchange cinebookExchange() {
        return new DirectExchange(EXCHANGE_CINEBOOK);
    }

    @Bean
    public Binding bookingNotificationBinding(Queue bookingNotificationsQueue,
                                              DirectExchange cinebookExchange) {
        return BindingBuilder.bind(bookingNotificationsQueue)
                .to(cinebookExchange)
                .with(ROUTING_KEY_BOOKING);
    }

    /** Serialize/deserialize messages as JSON using Jackson. */
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /** Inject the JSON converter into RabbitTemplate. */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }
}
