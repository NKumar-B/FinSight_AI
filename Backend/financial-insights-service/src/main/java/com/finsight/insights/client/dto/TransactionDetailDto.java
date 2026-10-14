package com.finsight.insights.client.dto;

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
public class TransactionDetailDto {
    private Long id;
    private Long accountId;
    private String accountNumber;
    private String transactionReference;
    private LocalDate transactionDate;
    private BigDecimal amount;
    private String currency;
    private String transactionType; // CREDIT, DEBIT
    private String description;
    private String category;
    private String merchantName;
    private String status;
}
