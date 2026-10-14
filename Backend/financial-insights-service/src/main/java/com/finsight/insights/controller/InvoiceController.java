package com.finsight.insights.controller;

import com.finsight.common.dto.ApiResponse;
import com.finsight.common.dto.PagedResponse;
import com.finsight.insights.dto.request.CreateInvoiceRequest;
import com.finsight.insights.dto.request.RecordPaymentRequest;
import com.finsight.insights.dto.response.AccountsReceivableAgingResponse;
import com.finsight.insights.dto.response.InvoiceResponse;
import com.finsight.insights.enums.InvoiceStatus;
import com.finsight.insights.service.InvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/insights/invoices")
@RequiredArgsConstructor
@Tag(name = "Invoices & AR Aging", description = "Endpoints for accounts receivable, invoice payments, and aging buckets")
public class InvoiceController {

    private final InvoiceService invoiceService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCIAL_ANALYST', 'ACCOUNTANT')")
    @Operation(summary = "Create invoice", description = "Issue a new customer invoice for accounts receivable tracking")
    public ResponseEntity<ApiResponse<InvoiceResponse>> createInvoice(
            @Valid @RequestBody CreateInvoiceRequest request,
            Authentication authentication) {
        String actor = authentication != null ? authentication.getName() : "ACCOUNTANT";
        InvoiceResponse response = invoiceService.createInvoice(request, actor);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response, "Invoice created successfully"));
    }

    @PostMapping("/{id}/payments")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCIAL_ANALYST', 'ACCOUNTANT')")
    @Operation(summary = "Record invoice payment", description = "Apply full or partial payment against an open invoice")
    public ResponseEntity<ApiResponse<InvoiceResponse>> recordPayment(
            @PathVariable("id") Long id,
            @Valid @RequestBody RecordPaymentRequest request,
            Authentication authentication) {
        String actor = authentication != null ? authentication.getName() : "ACCOUNTANT";
        InvoiceResponse response = invoiceService.recordPayment(id, request, actor);
        return ResponseEntity.ok(ApiResponse.ok(response, "Invoice payment recorded successfully"));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCIAL_ANALYST', 'ACCOUNTANT', 'AUDITOR')")
    @Operation(summary = "List invoices", description = "Returns paginated list of invoices, optionally filtered by status")
    public ResponseEntity<ApiResponse<PagedResponse<InvoiceResponse>>> listInvoices(
            @RequestParam(name = "status", required = false) InvoiceStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(invoiceService.listInvoices(status, pageable), "Invoices retrieved successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCIAL_ANALYST', 'ACCOUNTANT', 'AUDITOR')")
    @Operation(summary = "Get invoice by ID", description = "Retrieve single invoice details and balance due")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getInvoiceById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(ApiResponse.ok(invoiceService.getInvoiceById(id), "Invoice details retrieved"));
    }

    @GetMapping("/aging")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCIAL_ANALYST', 'ACCOUNTANT', 'AUDITOR')")
    @Operation(summary = "Get AR aging breakdown", description = "Calculates Accounts Receivable aging across 0-30, 31-60, 61-90, and 90+ day buckets")
    public ResponseEntity<ApiResponse<AccountsReceivableAgingResponse>> getArAging() {
        return ResponseEntity.ok(ApiResponse.ok(invoiceService.getArAging(), "AR aging report retrieved"));
    }
}
