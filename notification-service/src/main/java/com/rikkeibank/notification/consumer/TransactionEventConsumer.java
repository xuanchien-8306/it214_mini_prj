package com.rikkeibank.notification.consumer;

import com.rikkeibank.notification.entity.Notification;
import com.rikkeibank.notification.service.NotificationService;
import com.rikkeibank.transaction.event.TransactionSuccessEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionEventConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = "transaction-events", groupId = "notification-service-group")
    public void handleTransactionSuccessEvent(TransactionSuccessEvent event) {
        log.info("Received transaction success event: {}", event.getTransactionId());
        
        Notification notification = new Notification();
        notification.setTransactionId(event.getTransactionId());
        notification.setFromAccountNumber(event.getFromAccountNumber());
        notification.setToAccountNumber(event.getToAccountNumber());
        notification.setAmount(event.getAmount());
        notification.setCurrency(event.getCurrency());
        notification.setDescription(event.getDescription());
        notification.setMessage(String.format("Transfer of %s %s from %s to %s completed successfully",
                event.getAmount(), event.getCurrency(),
                event.getFromAccountNumber(), event.getToAccountNumber()));
        notification.setTimestamp(event.getTimestamp());
        notification.setRead(false);
        
        notificationService.createNotification(notification)
                .subscribe(
                        saved -> log.info("Notification saved: {}", saved.getId()),
                        error -> log.error("Failed to save notification: {}", error.getMessage())
                );
    }
}
