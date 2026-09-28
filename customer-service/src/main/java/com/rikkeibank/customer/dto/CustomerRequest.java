package com.rikkeibank.customer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CustomerRequest {
    @NotBlank
    private String fullName;
    
    @NotBlank
    private String identityNumber;
    
    @NotBlank
    private String phoneNumber;
    
    @NotBlank
    @Email
    private String email;
    
    @NotBlank
    private String address;
    
    @NotNull
    @Past
    private LocalDate dateOfBirth;
}
