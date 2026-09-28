package com.rikkeibank.notification.service;

import com.rikkeibank.notification.dto.NotificationResponse;
import com.rikkeibank.notification.entity.Notification;
import com.rikkeibank.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public Mono<Notification> createNotification(Notification notification) {
        return notificationRepository.save(notification);
    }

    public Flux<NotificationResponse> getNotificationsByAccountNumber(String accountNumber) {
        return notificationRepository.findByToAccountNumber(accountNumber)
                .mergeWith(notificationRepository.findByFromAccountNumber(accountNumber))
                .distinct()
                .map(this::mapToResponse);
    }

    public Flux<NotificationResponse> getAllNotifications() {
        return notificationRepository.findAll()
                .map(this::mapToResponse);
    }

    public Mono<Notification> markAsRead(String notificationId) {
        return notificationRepository.findById(notificationId)
                .flatMap(notification -> {
                    notification.setRead(true);
                    return notificationRepository.save(notification);
                });
    }

    private NotificationResponse mapToResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getTransactionId(),
                notification.getFromAccountNumber(),
                notification.getToAccountNumber(),
                notification.getAmount(),
                notification.getCurrency(),
                notification.getDescription(),
                notification.getMessage(),
                notification.getTimestamp(),
                notification.getRead()
        );
    }
}
