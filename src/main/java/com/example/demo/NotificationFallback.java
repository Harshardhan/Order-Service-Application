package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class NotificationFallback implements NotificationClient {

    private static final Logger logger = LoggerFactory.getLogger(NotificationFallback.class);

    @Override
    public NotificationRequest sendNotification(NotificationRequest notification) {
        logger.warn("Notification service is unavailable. Falling back...");
        NotificationRequest fallbackNotification = new NotificationRequest();
        fallbackNotification.setMessage("Fallback: Notification service is unavailable.");
        fallbackNotification.setCustomerId(notification.getCustomerId());
        return fallbackNotification;
    }
}
