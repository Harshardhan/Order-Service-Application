package com.example.demo;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String ORDER_NOTIFICATION_QUEUE = "order_notification";

    @Bean
    public Queue orderNotificationQueue() {
        return new Queue(ORDER_NOTIFICATION_QUEUE, false);
    }
}
