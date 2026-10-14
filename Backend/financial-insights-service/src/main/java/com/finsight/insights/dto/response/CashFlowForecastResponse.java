package com.finsight.insights.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CashFlowForecastResponse {
    private BigDecimal currentCashBalance;
    private int forecastDays;
    private BigDecimal projectedInflows;
    private BigDecimal projectedOutflows;
    private BigDecimal netProjectedCashFlow;
    private BigDecimal projectedClosingBalance;
    private BigDecimal estimatedBurnRatePerDay;
    private Long runwayDays;
    private List<DailyCashTrajectoryPoint> dailyTrajectory;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyCashTrajectoryPoint {
        private LocalDate date;
        private BigDecimal projectedBalance;
        private BigDecimal dailyNetChange;
    }
}
