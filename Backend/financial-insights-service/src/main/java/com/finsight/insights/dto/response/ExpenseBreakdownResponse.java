package com.finsight.insights.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseBreakdownResponse {
    private BigDecimal totalExpense;
    private String currency;
    private Map<String, BigDecimal> byCategory;
    private Map<String, Double> categoryPercentages;
    private Map<String, BigDecimal> topMerchants;
    private long totalTransactionsAnalyzed;
}
