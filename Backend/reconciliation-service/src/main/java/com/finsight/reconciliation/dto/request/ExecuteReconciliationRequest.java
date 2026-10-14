package com.finsight.reconciliation.dto.request;

import com.finsight.reconciliation.client.dto.ExternalTransactionDto;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecuteReconciliationRequest {

    @Min(value = 0, message = "Date tolerance days cannot be negative")
    @Max(value = 30, message = "Date tolerance days cannot exceed 30")
    @Builder.Default
    private int dateToleranceDays = 2;

    @Min(value = 0, message = "Amount tolerance cannot be negative")
    @Builder.Default
    private BigDecimal amountTolerance = new BigDecimal("0.05");

    private List<ExternalTransactionDto> externalCounterpartTransactions;
}
