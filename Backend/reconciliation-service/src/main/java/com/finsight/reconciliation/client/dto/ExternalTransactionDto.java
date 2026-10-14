package com.finsight.reconciliation.client.dto;

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
public class ExternalTransactionDto {
    private Long id;
    private Long accountId;
    private String accountNumber;
    private String transactionReference;
    private LocalDate transactionDate;
    private LocalDate valueDate;
    private BigDecimal amount;
    private String currency;
    private String transactionType;
    private String description;
    private String category;
    private String merchantName;
    private String counterpartAccount;
    private String status;
}
