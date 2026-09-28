package com.rikkeibank.notification.controller;

import com.rikkeibank.notification.dto.NotificationResponse;
import com.rikkeibank.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/account/{accountNumber}")
    public Flux<NotificationResponse> getNotificationsByAccount(@PathVariable String accountNumber) {
        return notificationService.getNotificationsByAccountNumber(accountNumber);
    }

    @GetMapping
    public Flux<NotificationResponse> getAllNotifications() {
        return notificationService.getAllNotifications();
    }

    @PutMapping("/{notificationId}/read")
    public Mono<Void> markAsRead(@PathVariable String notificationId) {
        return notificationService.markAsRead(notificationId).then();
    }
}
