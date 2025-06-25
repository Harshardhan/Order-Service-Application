package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderEventListener {

    private static final Logger logger = LoggerFactory.getLogger(OrderEventListener.class);

    @KafkaListener(
    	    topics = "${kafka.topic.order}",
    	    groupId = "order-group",
    	    containerFactory = "kafkaListenerContainerFactory"
    	)
    	public void consumeOrderEvent(Order order) {
    	    logger.info("📥 Received Order event: {}", order);
    	    System.out.println("📨 Sending notification to customer " + order.getCustomerId());
    	}

    	@KafkaListener(
    	    topics = "${kafka.topic.notification}",
    	    groupId = "order-group",
    	    containerFactory = "notificationKafkaListenerContainerFactory"
    	)
    	public void listenNotification(NotificationRequest notification) {
    	    String subject = "Order Confirmation: " + notification.getOrderReference();
    	    String body = "Hi, your order for " + notification.getProductName() + " has been placed successfully.";

    	    logger.info("📩 Notification for customerId: {}, Subject: {}", notification.getCustomerId(), subject);
    	    System.out.println("📩 Message: " + body);
    	}
}
