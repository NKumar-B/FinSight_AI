package com.finsight.insights.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountsReceivableAgingResponse {
    private BigDecimal totalOutstanding;
    private BigDecimal currentBucket;    // 0 - 30 days
    private BigDecimal bucket31To60;     // 31 - 60 days
    private BigDecimal bucket61To90;     // 61 - 90 days
    private BigDecimal bucket90Plus;     // > 90 days
    private long totalInvoicesCount;
    private long overdueInvoicesCount;
}
