package com.finsight.financial.controller;

import com.finsight.common.dto.ApiResponse;
import com.finsight.common.dto.PagedResponse;
import com.finsight.financial.dto.response.CsvImportResultResponse;
import com.finsight.financial.dto.response.ImportBatchResponse;
import com.finsight.financial.service.FinancialImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/financial/import")
@RequiredArgsConstructor
@Tag(name = "Financial Record Ingestion", description = "Endpoints for importing CSV statements, validating rows, and tracking batches")
public class FinancialImportController {

    private final FinancialImportService financialImportService;

    @PostMapping(value = "/csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCIAL_ANALYST')")
    @Operation(summary = "Import financial transactions via CSV", description = "Parses, validates, deduplicates, and normalizes financial records from an uploaded CSV file.")
    public ResponseEntity<ApiResponse<CsvImportResultResponse>> importCsv(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "accountId", required = false) Long accountId,
            Principal principal) {

        String importedBy = principal != null ? principal.getName() : "ANONYMOUS";
        CsvImportResultResponse result = financialImportService.importCsv(file, accountId, importedBy);

        HttpStatus status = result.getFailedRecords() > 0 && result.getSuccessfulRecords() == 0
                ? HttpStatus.UNPROCESSABLE_ENTITY
                : HttpStatus.CREATED;

        return new ResponseEntity<>(ApiResponse.ok(result, "CSV processed with " + result.getSuccessfulRecords()
                + " successes and " + result.getFailedRecords() + " failures"), status);
    }

    @GetMapping("/batches")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCIAL_ANALYST', 'COMPLIANCE_OFFICER', 'AUDITOR')")
    @Operation(summary = "List financial import batches", description = "Paginated list of historical CSV import batch executions.")
    public ResponseEntity<ApiResponse<PagedResponse<ImportBatchResponse>>> listBatches(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        PagedResponse<ImportBatchResponse> batches = financialImportService.listBatches(pageable);
        return ResponseEntity.ok(ApiResponse.ok(batches, "Batches retrieved successfully"));
    }

    @GetMapping("/batches/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCIAL_ANALYST', 'COMPLIANCE_OFFICER', 'AUDITOR')")
    @Operation(summary = "Get batch status and error details", description = "Detailed inspection of an import batch including row failure breakdown.")
    public ResponseEntity<ApiResponse<ImportBatchResponse>> getBatchById(@PathVariable("id") Long id) {
        ImportBatchResponse batch = financialImportService.getBatchById(id);
        return ResponseEntity.ok(ApiResponse.ok(batch, "Batch retrieved successfully"));
    }
}
