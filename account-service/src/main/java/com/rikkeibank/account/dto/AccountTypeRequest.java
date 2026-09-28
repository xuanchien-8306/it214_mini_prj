package com.rikkeibank.account.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AccountTypeRequest {
    @NotBlank
    private String code;
    
    @NotBlank
    private String name;
    
    private String description;
}
