package com.finsight.fraud.service;

import com.finsight.common.dto.ApiResponse;
import com.finsight.fraud.client.AuditClient;
import com.finsight.fraud.client.FinancialDataClient;
import com.finsight.fraud.client.dto.TransactionSummaryDto;
import com.finsight.fraud.dto.request.EvaluateTransactionRequest;
import com.finsight.fraud.dto.request.UpdateAlertStatusRequest;
import com.finsight.fraud.dto.response.FraudAlertResponse;
import com.finsight.fraud.dto.response.FraudEvaluationResponse;
import com.finsight.fraud.entity.FraudAlert;
import com.finsight.fraud.enums.AlertStatus;
import com.finsight.fraud.enums.FraudRule;
import com.finsight.fraud.enums.RiskSeverity;
import com.finsight.fraud.mapper.FraudMapper;
import com.finsight.fraud.repository.FraudAlertRepository;
import com.finsight.fraud.service.impl.FraudDetectionServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FraudDetectionServiceTest {

    @Mock
    private FraudAlertRepository alertRepository;

    @Mock
    private FinancialDataClient financialDataClient;

    @Mock
    private AuditClient auditClient;

    @Spy
    private FraudMapper mapper = new FraudMapper();

    @InjectMocks
    private FraudDetectionServiceImpl fraudService;

    @Test
    void evaluateTransaction_lowRisk_noAlertGenerated() {
        EvaluateTransactionRequest request = EvaluateTransactionRequest.builder()
                .transactionId(101L)
                .accountId(1L)
                .accountNumber("ACC-001")
                .transactionReference("TX-REGULAR-1")
                .amount(new BigDecimal("150.50"))
                .currency("USD")
                .transactionDate(LocalDate.of(2026, 4, 10))
                .international(false)
                .build();

        when(financialDataClient.getTransactionHistory(1L))
                .thenReturn(ApiResponse.ok(List.of(), "Empty history"));

        FraudEvaluationResponse response = fraudService.evaluateTransaction(request);

        assertThat(response).isNotNull();
        assertThat(response.getRiskScore()).isLessThan(30);
        assertThat(response.getSeverity()).isEqualTo(RiskSeverity.LOW);
        assertThat(response.isAlertGenerated()).isFalse();
        assertThat(response.getAlertReference()).isNull();

        verify(alertRepository, never()).save(any());
    }

    @Test
    void evaluateTransaction_extremeRisk_generatesAlert() {
        EvaluateTransactionRequest request = EvaluateTransactionRequest.builder()
                .transactionId(102L)
                .accountId(2L)
                .accountNumber("ACC-002")
                .transactionReference("TX-ANOMALY-1")
                .amount(new BigDecimal("60000.00")) // >= 50k (+40) and round multiple of 1000 (+15)
                .currency("EUR")
                .transactionDate(LocalDate.of(2026, 4, 10))
                .international(true) // international (+20) -> total score 75 (HIGH)
                .build();

        when(financialDataClient.getTransactionHistory(2L))
                .thenReturn(ApiResponse.ok(List.of(), "Empty history"));

        when(alertRepository.save(any(FraudAlert.class))).thenAnswer(i -> {
            FraudAlert a = i.getArgument(0);
            a.setId(99L);
            return a;
        });

        FraudEvaluationResponse response = fraudService.evaluateTransaction(request);

        assertThat(response).isNotNull();
        assertThat(response.getRiskScore()).isGreaterThanOrEqualTo(70);
        assertThat(response.getSeverity()).isIn(RiskSeverity.HIGH, RiskSeverity.CRITICAL);
        assertThat(response.isAlertGenerated()).isTrue();
        assertThat(response.getAlertReference()).startsWith("FRD-");
        assertThat(response.getTriggeredRules()).contains(
                FraudRule.HIGH_AMOUNT_ANOMALY.name(),
                FraudRule.SUSPICIOUS_ROUND_AMOUNT.name(),
                FraudRule.CROSS_BORDER_ANOMALY.name()
        );

        ArgumentCaptor<FraudAlert> captor = ArgumentCaptor.forClass(FraudAlert.class);
        verify(alertRepository).save(captor.capture());
        FraudAlert savedAlert = captor.getValue();
        assertThat(savedAlert.getStatus()).isEqualTo(AlertStatus.NEW);
        assertThat(savedAlert.getTransactionId()).isEqualTo(102L);
    }

    @Test
    void evaluateTransaction_highVelocity_triggersVelocityRule() {
        LocalDate today = LocalDate.of(2026, 4, 10);
        EvaluateTransactionRequest request = EvaluateTransactionRequest.builder()
                .transactionId(103L)
                .accountId(3L)
                .accountNumber("ACC-003")
                .transactionReference("TX-VEL-1")
                .amount(new BigDecimal("12000.00")) // +25 high amount
                .currency("USD")
                .transactionDate(today)
                .build();

        // 3 prior transactions on same date -> +25 velocity
        List<TransactionSummaryDto> history = List.of(
                TransactionSummaryDto.builder().id(201L).transactionDate(today).amount(new BigDecimal("100.00")).build(),
                TransactionSummaryDto.builder().id(202L).transactionDate(today).amount(new BigDecimal("200.00")).build(),
                TransactionSummaryDto.builder().id(203L).transactionDate(today).amount(new BigDecimal("300.00")).build()
        );
        when(financialDataClient.getTransactionHistory(3L))
                .thenReturn(ApiResponse.ok(history, "History"));

        when(alertRepository.save(any(FraudAlert.class))).thenAnswer(i -> {
            FraudAlert a = i.getArgument(0);
            a.setId(100L);
            return a;
        });

        FraudEvaluationResponse response = fraudService.evaluateTransaction(request);

        assertThat(response.getTriggeredRules()).contains(FraudRule.HIGH_VELOCITY_SPIKE.name());
        assertThat(response.getRiskScore()).isGreaterThanOrEqualTo(50);
        assertThat(response.isAlertGenerated()).isTrue();
    }

    @Test
    void updateAlertStatus_confirmedFraud_success() {
        FraudAlert existingAlert = FraudAlert.builder()
                .id(50L)
                .alertReference("FRD-5050")
                .transactionId(105L)
                .accountId(5L)
                .amount(new BigDecimal("25000.00"))
                .currency("USD")
                .riskScore(85)
                .severity(RiskSeverity.HIGH)
                .triggeredRules(FraudRule.HIGH_AMOUNT_ANOMALY.name())
                .status(AlertStatus.NEW)
                .build();

        when(alertRepository.findById(50L)).thenReturn(Optional.of(existingAlert));
        when(alertRepository.save(any(FraudAlert.class))).thenAnswer(i -> i.getArgument(0));

        UpdateAlertStatusRequest request = UpdateAlertStatusRequest.builder()
                .status(AlertStatus.CONFIRMED_FRAUD)
                .assignedTo("investigator_agent")
                .resolutionNotes("Confirmed compromised account via customer callback")
                .build();

        FraudAlertResponse response = fraudService.updateAlertStatus(50L, request, "investigator_agent");

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(AlertStatus.CONFIRMED_FRAUD);
        assertThat(response.getAssignedTo()).isEqualTo("investigator_agent");
        assertThat(response.getResolutionNotes()).contains("Confirmed compromised account");
        assertThat(existingAlert.getResolvedAt()).isNotNull();

        verify(alertRepository).save(existingAlert);
    }
}
