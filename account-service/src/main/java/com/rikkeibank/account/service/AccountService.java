package com.rikkeibank.account.service;

import com.rikkeibank.account.dto.AccountRequest;
import com.rikkeibank.account.dto.AccountResponse;
import com.rikkeibank.account.dto.BalanceUpdateRequest;
import com.rikkeibank.account.entity.Account;
import com.rikkeibank.account.entity.AccountType;
import com.rikkeibank.account.repository.AccountRepository;
import com.rikkeibank.account.repository.AccountTypeRepository;
import com.rikkeibank.common.exception.BusinessException;
import com.rikkeibank.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountTypeRepository accountTypeRepository;

    @CacheEvict(value = "accounts", allEntries = true)
    public AccountResponse createAccount(AccountRequest request) {
        if (accountRepository.existsByAccountNumber(request.getAccountNumber())) {
            throw new BusinessException("Account with this number already exists");
        }

        AccountType accountType = accountTypeRepository.findById(request.getAccountTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("AccountType", request.getAccountTypeId().toString()));

        Account account = new Account();
        account.setAccountNumber(request.getAccountNumber());
        account.setCustomerId(request.getCustomerId());
        account.setAccountType(accountType);
        account.setBalance(request.getInitialBalance());
        account.setCurrency(request.getCurrency());
        account.setActive(true);
        account.setCreatedAt(LocalDateTime.now());
        account.setUpdatedAt(LocalDateTime.now());

        account = accountRepository.save(account);
        return mapToResponse(account);
    }

    @Cacheable(value = "accounts", key = "#id")
    public AccountResponse getAccountById(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account", id.toString()));
        return mapToResponse(account);
    }

    @Cacheable(value = "accounts", key = "#accountNumber")
    public AccountResponse getAccountByNumber(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account", accountNumber));
        return mapToResponse(account);
    }

    @Cacheable(value = "accounts", key = "'customer:' + #customerId")
    public List<AccountResponse> getAccountsByCustomerId(Long customerId) {
        return accountRepository.findByCustomerId(customerId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "accounts", key = "'all'")
    public List<AccountResponse> getAllAccounts() {
        return accountRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(value = "accounts", allEntries = true)
    public AccountResponse debitAccount(String accountNumber, BigDecimal amount) {
        Account account = accountRepository.findByAccountNumberForUpdate(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account", accountNumber));

        if (account.getBalance().compareTo(amount) < 0) {
            throw new BusinessException("Insufficient balance");
        }

        account.setBalance(account.getBalance().subtract(amount));
        account.setUpdatedAt(LocalDateTime.now());

        account = accountRepository.save(account);
        return mapToResponse(account);
    }

    @Transactional
    @CacheEvict(value = "accounts", allEntries = true)
    public AccountResponse creditAccount(String accountNumber, BigDecimal amount) {
        Account account = accountRepository.findByAccountNumberForUpdate(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account", accountNumber));

        account.setBalance(account.getBalance().add(amount));
        account.setUpdatedAt(LocalDateTime.now());

        account = accountRepository.save(account);
        return mapToResponse(account);
    }

    @CacheEvict(value = "accounts", allEntries = true)
    public void deleteAccount(Long id) {
        if (!accountRepository.existsById(id)) {
            throw new ResourceNotFoundException("Account", id.toString());
        }
        accountRepository.deleteById(id);
    }

    private AccountResponse mapToResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getCustomerId(),
                account.getAccountType() != null ? account.getAccountType().getId() : null,
                account.getAccountType() != null ? account.getAccountType().getName() : null,
                account.getBalance(),
                account.getCurrency(),
                account.getActive(),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }
}
