package com.example.demo;
import org.springframework.stereotype.Component;


@Component
public class NotificationFallback implements NotificationClient {

    @Override
    public NotificationRequest sendNotification(NotificationRequest notification) {
        NotificationRequest fallbackNotification = new NotificationRequest();
        fallbackNotification.setMessage("Fallback: Notification service is unavailable.");
        fallbackNotification.setCustomerId(notification.getCustomerId());
        return fallbackNotification;
    }

}
