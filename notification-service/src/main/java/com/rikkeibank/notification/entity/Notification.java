package com.rikkeibank.notification.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notifications")
public class Notification {

    @Id
    private String id;

    private String transactionId;

    private String fromAccountNumber;

    private String toAccountNumber;

    private BigDecimal amount;

    private String currency;

    private String description;

    private String message;

    private LocalDateTime timestamp;

    private Boolean read = false;
}
