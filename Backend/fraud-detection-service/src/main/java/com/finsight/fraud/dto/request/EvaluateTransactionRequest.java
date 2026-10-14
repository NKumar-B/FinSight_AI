package com.finsight.fraud.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluateTransactionRequest {

    @NotNull(message = "Transaction ID is required")
    private Long transactionId;

    @NotNull(message = "Account ID is required")
    private Long accountId;

    private String accountNumber;

    @NotBlank(message = "Transaction reference is required")
    private String transactionReference;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be strictly positive")
    private BigDecimal amount;

    @NotBlank(message = "Currency code is required")
    private String currency;

    private LocalDate transactionDate;

    private String category;

    private String merchantName;

    private String counterpartAccount;

    @Builder.Default
    private boolean international = false;
}
