package com.finsight.financial.dto.response;

import com.finsight.financial.enums.TransactionStatus;
import com.finsight.financial.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private Long id;
    private Long accountId;
    private String accountNumber;
    private String transactionReference;
    private LocalDate transactionDate;
    private LocalDate valueDate;
    private BigDecimal amount;
    private String currency;
    private TransactionType transactionType;
    private String description;
    private String category;
    private String merchantName;
    private String counterpartAccount;
    private TransactionStatus status;
    private Long importBatchId;
    private Instant createdAt;
    private Instant updatedAt;
}
