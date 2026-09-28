package com.rikkeibank.customer.dto;

import com.rikkeibank.common.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StaffRequest {
    @NotBlank
    private String fullName;
    
    @NotBlank
    private String employeeCode;
    
    @NotBlank
    private String phoneNumber;
    
    @NotBlank
    @Email
    private String email;
    
    @NotNull
    private Role role;
}
