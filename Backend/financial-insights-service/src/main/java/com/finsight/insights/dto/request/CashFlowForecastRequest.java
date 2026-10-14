package com.finsight.insights.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CashFlowForecastRequest {

    @Min(value = 7, message = "Forecast window must be at least 7 days")
    @Max(value = 365, message = "Forecast window cannot exceed 365 days")
    @Builder.Default
    private int days = 30;

    @Builder.Default
    private BigDecimal monthlyRecurringInflow = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal monthlyRecurringOutflow = BigDecimal.ZERO;
}
