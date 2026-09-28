package com.rikkeibank.transaction.client;

import com.rikkeibank.account.dto.AccountResponse;
import com.rikkeibank.account.dto.BalanceUpdateRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@FeignClient(name = "account-service")
@CircuitBreaker(name = "accountService", fallbackMethod = "accountFallback")
public interface AccountClient {

    @GetMapping("/api/v1/accounts/number/{accountNumber}")
    AccountResponse getAccountByNumber(@PathVariable String accountNumber);

    @PostMapping("/api/v1/accounts/debit")
    AccountResponse debitAccount(@RequestBody BalanceUpdateRequest request);

    @PostMapping("/api/v1/accounts/credit")
    AccountResponse creditAccount(@RequestBody BalanceUpdateRequest request);

    default AccountResponse accountFallback(String accountNumber, Throwable throwable) {
        throw new RuntimeException("Account service is unavailable");
    }

    default AccountResponse accountFallback(BalanceUpdateRequest request, Throwable throwable) {
        throw new RuntimeException("Account service is unavailable");
    }
}
