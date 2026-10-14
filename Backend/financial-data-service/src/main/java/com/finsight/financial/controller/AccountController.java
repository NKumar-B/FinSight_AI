package com.finsight.financial.controller;

import com.finsight.common.dto.ApiResponse;
import com.finsight.common.dto.PagedResponse;
import com.finsight.financial.dto.request.CreateAccountRequest;
import com.finsight.financial.dto.response.AccountResponse;
import com.finsight.financial.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@Tag(name = "Accounts", description = "Endpoints for managing institutional and bank accounts")
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCIAL_ANALYST')")
    @Operation(summary = "Create a new financial account", description = "Initializes an account with unique number, type, and currency.")
    public ResponseEntity<ApiResponse<AccountResponse>> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        AccountResponse response = accountService.createAccount(request);
        return new ResponseEntity<>(ApiResponse.created(response, "Account created successfully"), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCIAL_ANALYST', 'COMPLIANCE_OFFICER', 'AUDITOR', 'REVIEWER', 'USER')")
    @Operation(summary = "List accounts with search and pagination", description = "Retrieves all accounts with optional name/number search.")
    public ResponseEntity<ApiResponse<PagedResponse<AccountResponse>>> listAccounts(
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(name = "direction", defaultValue = "asc") String direction) {

        Sort sort = "desc".equalsIgnoreCase(direction) ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        PagedResponse<AccountResponse> response = accountService.listAccounts(search, pageable);
        return ResponseEntity.ok(ApiResponse.ok(response, "Accounts retrieved successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCIAL_ANALYST', 'COMPLIANCE_OFFICER', 'AUDITOR', 'REVIEWER', 'USER')")
    @Operation(summary = "Get account details by ID", description = "Retrieves specific account metadata and current balance.")
    public ResponseEntity<ApiResponse<AccountResponse>> getAccountById(@PathVariable("id") Long id) {
        AccountResponse response = accountService.getAccountById(id);
        return ResponseEntity.ok(ApiResponse.ok(response, "Account retrieved successfully"));
    }

    @GetMapping("/by-number/{accountNumber}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCIAL_ANALYST', 'COMPLIANCE_OFFICER', 'AUDITOR', 'REVIEWER', 'USER')")
    @Operation(summary = "Get account by account number", description = "Looks up an account by its unique account number.")
    public ResponseEntity<ApiResponse<AccountResponse>> getAccountByNumber(@PathVariable("accountNumber") String accountNumber) {
        AccountResponse response = accountService.getAccountByNumber(accountNumber);
        return ResponseEntity.ok(ApiResponse.ok(response, "Account retrieved successfully"));
    }
}
