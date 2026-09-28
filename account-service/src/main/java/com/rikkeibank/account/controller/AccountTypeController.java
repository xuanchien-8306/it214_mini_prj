package com.rikkeibank.account.controller;

import com.rikkeibank.account.dto.AccountTypeRequest;
import com.rikkeibank.account.dto.AccountTypeResponse;
import com.rikkeibank.account.service.AccountTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/account-types")
@RequiredArgsConstructor
public class AccountTypeController {

    private final AccountTypeService accountTypeService;

    @PostMapping
    public ResponseEntity<AccountTypeResponse> createAccountType(@Valid @RequestBody AccountTypeRequest request) {
        return ResponseEntity.ok(accountTypeService.createAccountType(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountTypeResponse> getAccountType(@PathVariable Long id) {
        return ResponseEntity.ok(accountTypeService.getAccountTypeById(id));
    }

    @GetMapping
    public ResponseEntity<List<AccountTypeResponse>> getAllAccountTypes() {
        return ResponseEntity.ok(accountTypeService.getAllAccountTypes());
    }

    @PutMapping("/{id}")
    public ResponseEntity<AccountTypeResponse> updateAccountType(@PathVariable Long id,
                                                                 @Valid @RequestBody AccountTypeRequest request) {
        return ResponseEntity.ok(accountTypeService.updateAccountType(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccountType(@PathVariable Long id) {
        accountTypeService.deleteAccountType(id);
        return ResponseEntity.noContent().build();
    }
}
