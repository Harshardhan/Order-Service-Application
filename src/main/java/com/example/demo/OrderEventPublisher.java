package com.example.demo;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderEventPublisher {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendOrderEvent(String message) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.ORDER_NOTIFICATION_QUEUE, message);
        System.out.println("Sent message to RabbitMQ: " + message);
    }
}
