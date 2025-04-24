package com.example.demo;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "NOTIFICATION-SERVICE", fallback = NotificationFallback.class)
public interface NotificationClient {

    @PostMapping("/api/notifications/send")
    Notification sendNotification(@RequestBody Notification notification);

}
