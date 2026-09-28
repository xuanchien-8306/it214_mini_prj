package com.rikkeibank.transaction.service;

import com.rikkeibank.account.dto.AccountResponse;
import com.rikkeibank.account.dto.BalanceUpdateRequest;
import com.rikkeibank.common.enums.TransactionStatus;
import com.rikkeibank.transaction.client.AccountClient;
import com.rikkeibank.transaction.entity.Transaction;
import com.rikkeibank.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SagaOrchestrator {

    private final TransactionRepository transactionRepository;
    private final AccountClient accountClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String TRANSACTION_EVENTS_TOPIC = "transaction-events";

    @Transactional
    public Transaction executeTransfer(String fromAccountNumber, String toAccountNumber, 
                                       BigDecimal amount, String currency, String description) {
        String transactionId = UUID.randomUUID().toString();
        
        Transaction transaction = new Transaction();
        transaction.setTransactionId(transactionId);
        transaction.setFromAccountNumber(fromAccountNumber);
        transaction.setToAccountNumber(toAccountNumber);
        transaction.setAmount(amount);
        transaction.setCurrency(currency);
        transaction.setDescription(description);
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setUpdatedAt(LocalDateTime.now());
        
        transaction = transactionRepository.save(transaction);
        
        try {
            // Step 1: Debit from account
            log.info("Step 1: Debiting {} from account {}", amount, fromAccountNumber);
            BalanceUpdateRequest debitRequest = new BalanceUpdateRequest();
            debitRequest.setAccountNumber(fromAccountNumber);
            debitRequest.setAmount(amount);
            accountClient.debitAccount(debitRequest);
            
            // Step 2: Credit to account
            log.info("Step 2: Crediting {} to account {}", amount, toAccountNumber);
            BalanceUpdateRequest creditRequest = new BalanceUpdateRequest();
            creditRequest.setAccountNumber(toAccountNumber);
            creditRequest.setAmount(amount);
            accountClient.creditAccount(creditRequest);
            
            // Step 3: Update transaction status to COMPLETED
            transaction.setStatus(TransactionStatus.COMPLETED);
            transaction.setUpdatedAt(LocalDateTime.now());
            transaction = transactionRepository.save(transaction);
            
            // Step 4: Publish success event to Kafka
            publishSuccessEvent(transaction);
            
            log.info("Transfer completed成功: {}", transactionId);
            return transaction;
            
        } catch (Exception e) {
            log.error("Transfer failed, initiating compensation: {}", e.getMessage());
            
            // Compensating transaction: refund the debited amount
            try {
                log.info("Compensating: Refunding {} to account {}", amount, fromAccountNumber);
                BalanceUpdateRequest refundRequest = new BalanceUpdateRequest();
                refundRequest.setAccountNumber(fromAccountNumber);
                refundRequest.setAmount(amount);
                accountClient.creditAccount(refundRequest);
                
                transaction.setStatus(TransactionStatus.ROLLED_BACK);
                transaction.setFailureReason("Transfer failed and amount was refunded");
            } catch (Exception compensationException) {
                log.error("Compensation failed: {}", compensationException.getMessage());
                transaction.setStatus(TransactionStatus.FAILED);
                transaction.setFailureReason("Transfer failed and compensation also failed: " + compensationException.getMessage());
            }
            
            transaction.setUpdatedAt(LocalDateTime.now());
            transaction = transactionRepository.save(transaction);
            
            return transaction;
        }
    }

    private void publishSuccessEvent(Transaction transaction) {
        try {
            var event = new com.rikkeibank.transaction.event.TransactionSuccessEvent(
                    transaction.getTransactionId(),
                    transaction.getFromAccountNumber(),
                    transaction.getToAccountNumber(),
                    transaction.getAmount(),
                    transaction.getCurrency(),
                    transaction.getDescription(),
                    LocalDateTime.now()
            );
            kafkaTemplate.send(TRANSACTION_EVENTS_TOPIC, transaction.getTransactionId(), event);
            log.info("Published transaction success event: {}", transaction.getTransactionId());
        } catch (Exception e) {
            log.error("Failed to publish transaction event: {}", e.getMessage());
        }
    }
}
