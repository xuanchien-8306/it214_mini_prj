package com.rikkeibank.notification.repository;

import com.rikkeibank.notification.entity.Notification;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface NotificationRepository extends ReactiveMongoRepository<Notification, String> {
    Flux<Notification> findByToAccountNumber(String toAccountNumber);
    Flux<Notification> findByFromAccountNumber(String fromAccountNumber);
}
