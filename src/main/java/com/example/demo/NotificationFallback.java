package com.example.demo;

import org.springframework.stereotype.Component;

@Component
public class NotificationFallback implements NotificationClient {

    @Override
    public Notification sendNotification(Notification notification) {
        Notification fallbackNotification = new Notification();
        fallbackNotification.setMessage("Fallback: Notification service is unavailable.");
        fallbackNotification.setCustomerId(notification.getCustomerId());
        return fallbackNotification;
    }
}
