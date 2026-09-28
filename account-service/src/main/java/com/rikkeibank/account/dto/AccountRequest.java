package com.rikkeibank.account.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AccountRequest {
    @NotBlank
    private String accountNumber;
    
    @NotNull
    private Long customerId;
    
    @NotNull
    private Long accountTypeId;
    
    @NotNull
    @Positive
    private BigDecimal initialBalance;
    
    @NotBlank
    private String currency;
}
