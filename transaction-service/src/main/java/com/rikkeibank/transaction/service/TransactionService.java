package com.rikkeibank.transaction.service;

import com.rikkeibank.account.dto.AccountResponse;
import com.rikkeibank.common.exception.BusinessException;
import com.rikkeibank.common.exception.ResourceNotFoundException;
import com.rikkeibank.transaction.client.AccountClient;
import com.rikkeibank.transaction.dto.TransferRequest;
import com.rikkeibank.transaction.dto.TransactionResponse;
import com.rikkeibank.transaction.entity.Transaction;
import com.rikkeibank.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final SagaOrchestrator sagaOrchestrator;
    private final AccountClient accountClient;

    public TransactionResponse transfer(TransferRequest request) {
        // Validate accounts exist
        AccountResponse fromAccount = accountClient.getAccountByNumber(request.getFromAccountNumber());
        AccountResponse toAccount = accountClient.getAccountByNumber(request.getToAccountNumber());
        
        if (fromAccount == null) {
            throw new ResourceNotFoundException("Account", request.getFromAccountNumber());
        }
        if (toAccount == null) {
            throw new ResourceNotFoundException("Account", request.getToAccountNumber());
        }
        
        // Validate currency
        if (!fromAccount.getCurrency().equals(request.getCurrency()) || 
            !toAccount.getCurrency().equals(request.getCurrency())) {
            throw new BusinessException("Currency mismatch");
        }
        
        // Validate amount
        if (request.getAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Amount must be positive");
        }
        
        Transaction transaction = sagaOrchestrator.executeTransfer(
                request.getFromAccountNumber(),
                request.getToAccountNumber(),
                request.getAmount(),
                request.getCurrency(),
                request.getDescription()
        );
        
        return mapToResponse(transaction);
    }

    public TransactionResponse getTransactionById(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", id.toString()));
        return mapToResponse(transaction);
    }

    public TransactionResponse getTransactionByTransactionId(String transactionId) {
        Transaction transaction = transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", transactionId));
        return mapToResponse(transaction);
    }

    public List<TransactionResponse> getTransactionsByAccountNumber(String accountNumber) {
        return transactionRepository.findByFromAccountNumberOrToAccountNumber(accountNumber, accountNumber)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<TransactionResponse> getAllTransactions() {
        return transactionRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private TransactionResponse mapToResponse(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getTransactionId(),
                transaction.getFromAccountNumber(),
                transaction.getToAccountNumber(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getDescription(),
                transaction.getStatus(),
                transaction.getFailureReason(),
                transaction.getCreatedAt(),
                transaction.getUpdatedAt()
        );
    }
}
