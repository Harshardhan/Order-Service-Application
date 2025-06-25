package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class OrderEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(OrderEventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topic.order}")
    private String orderTopic;

    @Value("${kafka.topic.notification}")
    private String notificationTopic;

    public OrderEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishOrderPlacedEvent(Order order) {
        kafkaTemplate.send(orderTopic, order.getCustomerId().toString(), order);
        logger.info("✅ Kafka Order Event published to topic: {}", orderTopic);
    }

    public void publishNotificationEvent(NotificationRequest notification) {
    	kafkaTemplate.send(notificationTopic, String.valueOf(notification.getCustomerId()), notification);
        logger.info("✅ Kafka Notification Event published to topic: {}", notificationTopic);
    }
}
