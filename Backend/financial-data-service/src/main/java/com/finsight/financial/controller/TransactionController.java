package com.finsight.financial.controller;

import com.finsight.common.dto.ApiResponse;
import com.finsight.common.dto.PagedResponse;
import com.finsight.financial.dto.request.CreateTransactionRequest;
import com.finsight.financial.dto.request.UpdateTransactionStatusRequest;
import com.finsight.financial.dto.response.TransactionResponse;
import com.finsight.financial.enums.TransactionStatus;
import com.finsight.financial.enums.TransactionType;
import com.finsight.financial.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Endpoints for managing financial ledger transactions")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCIAL_ANALYST')")
    @Operation(summary = "Post a single transaction", description = "Creates and posts a new ledger transaction, updating account balance.")
    public ResponseEntity<ApiResponse<TransactionResponse>> createTransaction(@Valid @RequestBody CreateTransactionRequest request) {
        TransactionResponse response = transactionService.createTransaction(request);
        return new ResponseEntity<>(ApiResponse.created(response, "Transaction posted successfully"), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCIAL_ANALYST', 'COMPLIANCE_OFFICER', 'AUDITOR', 'REVIEWER', 'USER')")
    @Operation(summary = "Search and filter transactions", description = "Filter transactions by account, date range, amounts, type, status, and category.")
    public ResponseEntity<ApiResponse<PagedResponse<TransactionResponse>>> searchTransactions(
            @RequestParam(name = "accountId", required = false) Long accountId,
            @RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(name = "minAmount", required = false) BigDecimal minAmount,
            @RequestParam(name = "maxAmount", required = false) BigDecimal maxAmount,
            @RequestParam(name = "type", required = false) TransactionType type,
            @RequestParam(name = "status", required = false) TransactionStatus status,
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "transactionDate") String sortBy,
            @RequestParam(name = "direction", defaultValue = "desc") String direction) {

        Sort sort = "asc".equalsIgnoreCase(direction) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        PagedResponse<TransactionResponse> response = transactionService.searchTransactions(
                accountId, startDate, endDate, minAmount, maxAmount, type, status, category, search, pageable);
        return ResponseEntity.ok(ApiResponse.ok(response, "Transactions retrieved successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCIAL_ANALYST', 'COMPLIANCE_OFFICER', 'AUDITOR', 'REVIEWER', 'USER')")
    @Operation(summary = "Get transaction by ID", description = "Retrieves full transaction details by ID.")
    public ResponseEntity<ApiResponse<TransactionResponse>> getTransactionById(@PathVariable("id") Long id) {
        TransactionResponse response = transactionService.getTransactionById(id);
        return ResponseEntity.ok(ApiResponse.ok(response, "Transaction retrieved successfully"));
    }

    @GetMapping("/by-reference/{reference}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCIAL_ANALYST', 'COMPLIANCE_OFFICER', 'AUDITOR', 'REVIEWER', 'USER')")
    @Operation(summary = "Get transaction by reference", description = "Finds a transaction by its business reference identifier.")
    public ResponseEntity<ApiResponse<TransactionResponse>> getTransactionByReference(@PathVariable("reference") String reference) {
        TransactionResponse response = transactionService.getTransactionByReference(reference);
        return ResponseEntity.ok(ApiResponse.ok(response, "Transaction retrieved successfully"));
    }

    @GetMapping("/unreconciled")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCIAL_ANALYST', 'REVIEWER', 'COMPLIANCE_OFFICER')")
    @Operation(summary = "Get unreconciled transactions", description = "Specialized endpoint for Reconciliation Service to fetch unreconciled ledger entries.")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getUnreconciledTransactions() {
        List<TransactionResponse> response = transactionService.getUnreconciledTransactions();
        return ResponseEntity.ok(ApiResponse.ok(response, "Unreconciled transactions retrieved successfully"));
    }

    @GetMapping("/history")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCIAL_ANALYST', 'COMPLIANCE_OFFICER')")
    @Operation(summary = "Get transaction history for account", description = "Specialized endpoint for Fraud Detection Service to fetch transaction window history.")
    public ResponseEntity<ApiResponse<PagedResponse<TransactionResponse>>> getTransactionHistory(
            @RequestParam(name = "accountId") Long accountId,
            @RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "50") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("transactionDate").descending());
        PagedResponse<TransactionResponse> response = transactionService.getTransactionHistory(accountId, startDate, endDate, pageable);
        return ResponseEntity.ok(ApiResponse.ok(response, "Transaction history retrieved successfully"));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCIAL_ANALYST', 'REVIEWER')")
    @Operation(summary = "Update transaction status", description = "Updates status e.g. when reconciled or flagged during discrepancy review.")
    public ResponseEntity<ApiResponse<TransactionResponse>> updateStatus(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateTransactionStatusRequest request) {
        TransactionResponse response = transactionService.updateTransactionStatus(id, request);
        return ResponseEntity.ok(ApiResponse.ok(response, "Transaction status updated successfully"));
    }
}
