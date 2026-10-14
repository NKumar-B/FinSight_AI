package com.finsight.fraud.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudAlertStatsResponse {
    private long totalAlerts;
    private long newAlerts;
    private long underReviewAlerts;
    private long confirmedFraudAlerts;
    private long falsePositiveAlerts;
    private long dismissedAlerts;
    private long criticalAlerts;
    private long highAlerts;
    private long mediumAlerts;
    private long lowAlerts;
}
