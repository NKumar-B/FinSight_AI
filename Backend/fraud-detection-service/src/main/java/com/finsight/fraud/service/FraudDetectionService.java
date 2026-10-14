package com.finsight.fraud.service;

import com.finsight.common.dto.PagedResponse;
import com.finsight.fraud.dto.request.EvaluateTransactionRequest;
import com.finsight.fraud.dto.request.UpdateAlertStatusRequest;
import com.finsight.fraud.dto.response.FraudAlertResponse;
import com.finsight.fraud.dto.response.FraudAlertStatsResponse;
import com.finsight.fraud.dto.response.FraudEvaluationResponse;
import com.finsight.fraud.enums.AlertStatus;
import com.finsight.fraud.enums.RiskSeverity;
import org.springframework.data.domain.Pageable;

public interface FraudDetectionService {
    FraudEvaluationResponse evaluateTransaction(EvaluateTransactionRequest request);
    PagedResponse<FraudAlertResponse> listAlerts(AlertStatus status, RiskSeverity severity, Pageable pageable);
    FraudAlertResponse getAlertById(Long id);
    FraudAlertResponse updateAlertStatus(Long id, UpdateAlertStatusRequest request, String investigator);
    FraudAlertStatsResponse getAlertStats();
}
