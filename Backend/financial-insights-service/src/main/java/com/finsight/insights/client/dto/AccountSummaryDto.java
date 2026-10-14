package com.finsight.insights.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountSummaryDto {
    private Long id;
    private String accountNumber;
    private String accountName;
    private String accountType;
    private BigDecimal currentBalance;
    private String currency;
    private String status;
}
