package com.rikkeibank.account.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BalanceUpdateRequest {
    @NotNull
    private String accountNumber;
    
    @NotNull
    private BigDecimal amount;
}
