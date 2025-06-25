package com.example.demo;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

//Feign Client interface to communicate with the Notification Service

@FeignClient(name = "NOTIFICATION-SERVICE", fallback = NotificationFallback.class)
public interface NotificationClient {

    @PostMapping("/api/notifications/send")
    NotificationRequest sendNotification(@RequestBody NotificationRequest notification);

}