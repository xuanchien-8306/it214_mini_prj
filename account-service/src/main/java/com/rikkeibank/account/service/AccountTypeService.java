package com.rikkeibank.account.service;

import com.rikkeibank.account.dto.AccountTypeRequest;
import com.rikkeibank.account.dto.AccountTypeResponse;
import com.rikkeibank.account.entity.AccountType;
import com.rikkeibank.account.repository.AccountTypeRepository;
import com.rikkeibank.common.exception.BusinessException;
import com.rikkeibank.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountTypeService {

    private final AccountTypeRepository accountTypeRepository;

    @CacheEvict(value = "accountTypes", allEntries = true)
    public AccountTypeResponse createAccountType(AccountTypeRequest request) {
        if (accountTypeRepository.existsByCode(request.getCode())) {
            throw new BusinessException("Account type with this code already exists");
        }

        AccountType accountType = new AccountType();
        accountType.setCode(request.getCode());
        accountType.setName(request.getName());
        accountType.setDescription(request.getDescription());
        accountType.setCreatedAt(LocalDateTime.now());
        accountType.setUpdatedAt(LocalDateTime.now());

        accountType = accountTypeRepository.save(accountType);
        return mapToResponse(accountType);
    }

    @Cacheable(value = "accountTypes", key = "#id")
    public AccountTypeResponse getAccountTypeById(Long id) {
        AccountType accountType = accountTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AccountType", id.toString()));
        return mapToResponse(accountType);
    }

    @Cacheable(value = "accountTypes", key = "'all'")
    public List<AccountTypeResponse> getAllAccountTypes() {
        return accountTypeRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @CacheEvict(value = "accountTypes", allEntries = true)
    public AccountTypeResponse updateAccountType(Long id, AccountTypeRequest request) {
        AccountType accountType = accountTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AccountType", id.toString()));

        if (!accountType.getCode().equals(request.getCode()) &&
            accountTypeRepository.existsByCode(request.getCode())) {
            throw new BusinessException("Account type with this code already exists");
        }

        accountType.setCode(request.getCode());
        accountType.setName(request.getName());
        accountType.setDescription(request.getDescription());
        accountType.setUpdatedAt(LocalDateTime.now());

        accountType = accountTypeRepository.save(accountType);
        return mapToResponse(accountType);
    }

    @CacheEvict(value = "accountTypes", allEntries = true)
    public void deleteAccountType(Long id) {
        if (!accountTypeRepository.existsById(id)) {
            throw new ResourceNotFoundException("AccountType", id.toString());
        }
        accountTypeRepository.deleteById(id);
    }

    private AccountTypeResponse mapToResponse(AccountType accountType) {
        return new AccountTypeResponse(
                accountType.getId(),
                accountType.getCode(),
                accountType.getName(),
                accountType.getDescription(),
                accountType.getCreatedAt(),
                accountType.getUpdatedAt()
        );
    }
}
