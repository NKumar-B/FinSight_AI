package com.finsight.reconciliation.service;

import com.finsight.common.dto.ApiResponse;
import com.finsight.reconciliation.client.AuditClient;
import com.finsight.reconciliation.client.FinancialDataClient;
import com.finsight.reconciliation.client.dto.ExternalTransactionDto;
import com.finsight.reconciliation.dto.request.ExecuteReconciliationRequest;
import com.finsight.reconciliation.dto.response.ReconciliationRunResponse;
import com.finsight.reconciliation.entity.ReconciliationDiscrepancy;
import com.finsight.reconciliation.entity.ReconciliationMatch;
import com.finsight.reconciliation.entity.ReconciliationRun;
import com.finsight.reconciliation.enums.DiscrepancyType;
import com.finsight.reconciliation.enums.MatchType;
import com.finsight.reconciliation.mapper.ReconciliationMapper;
import com.finsight.reconciliation.repository.ReconciliationDiscrepancyRepository;
import com.finsight.reconciliation.repository.ReconciliationMatchRepository;
import com.finsight.reconciliation.repository.ReconciliationRunRepository;
import com.finsight.reconciliation.service.impl.ReconciliationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReconciliationServiceTest {

    @Mock
    private ReconciliationRunRepository runRepository;

    @Mock
    private ReconciliationMatchRepository matchRepository;

    @Mock
    private ReconciliationDiscrepancyRepository discrepancyRepository;

    @Mock
    private FinancialDataClient financialDataClient;

    @Mock
    private AuditClient auditClient;

    @Spy
    private ReconciliationMapper mapper = new ReconciliationMapper();

    @InjectMocks
    private ReconciliationServiceImpl reconciliationService;

    @BeforeEach
    void setUp() {
        when(runRepository.save(any(ReconciliationRun.class))).thenAnswer(invocation -> {
            ReconciliationRun run = invocation.getArgument(0);
            if (run.getId() == null) {
                run.setId(1L);
            }
            return run;
        });
    }

    @Test
    void executeReconciliation_exactMatch_success() {
        // Source transaction from financial data service
        ExternalTransactionDto sourceTx = ExternalTransactionDto.builder()
                .id(101L)
                .transactionReference("TX-EXACT-001")
                .amount(new BigDecimal("1500.00"))
                .transactionDate(LocalDate.of(2026, 1, 15))
                .build();
        when(financialDataClient.getUnreconciledTransactions())
                .thenReturn(ApiResponse.ok(List.of(sourceTx), "Unreconciled list"));

        // Target counterpart transaction
        ExternalTransactionDto targetTx = ExternalTransactionDto.builder()
                .id(201L)
                .transactionReference("TX-EXACT-001")
                .amount(new BigDecimal("1500.00"))
                .transactionDate(LocalDate.of(2026, 1, 15))
                .build();

        ExecuteReconciliationRequest request = ExecuteReconciliationRequest.builder()
                .externalCounterpartTransactions(List.of(targetTx))
                .build();

        ReconciliationRunResponse response = reconciliationService.executeReconciliation(request, "test-user");

        assertThat(response).isNotNull();
        assertThat(response.getExactMatchesCount()).isEqualTo(1);
        assertThat(response.getDiscrepanciesCount()).isEqualTo(0);

        // Verify match saved with EXACT type
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ReconciliationMatch>> matchCaptor = ArgumentCaptor.forClass(List.class);
        verify(matchRepository).saveAll(matchCaptor.capture());
        List<ReconciliationMatch> savedMatches = matchCaptor.getValue();
        assertThat(savedMatches).hasSize(1);
        assertThat(savedMatches.get(0).getMatchType()).isEqualTo(MatchType.EXACT);

        // Verify status update called
        verify(financialDataClient).updateTransactionStatus(eq(101L), any());
    }

    @Test
    void executeReconciliation_toleranceMatch_success() {
        // 1 day difference and $0.02 amount difference
        ExternalTransactionDto sourceTx = ExternalTransactionDto.builder()
                .id(102L)
                .transactionReference("TX-TOL-001")
                .amount(new BigDecimal("250.00"))
                .transactionDate(LocalDate.of(2026, 2, 10))
                .build();
        when(financialDataClient.getUnreconciledTransactions())
                .thenReturn(ApiResponse.ok(List.of(sourceTx), "Unreconciled list"));

        ExternalTransactionDto targetTx = ExternalTransactionDto.builder()
                .id(202L)
                .transactionReference("TX-TOL-001")
                .amount(new BigDecimal("250.02"))
                .transactionDate(LocalDate.of(2026, 2, 11))
                .build();

        ExecuteReconciliationRequest request = ExecuteReconciliationRequest.builder()
                .dateToleranceDays(2)
                .amountTolerance(new BigDecimal("0.05"))
                .externalCounterpartTransactions(List.of(targetTx))
                .build();

        ReconciliationRunResponse response = reconciliationService.executeReconciliation(request, "test-user");

        assertThat(response).isNotNull();
        assertThat(response.getToleranceMatchesCount()).isEqualTo(1);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ReconciliationMatch>> matchCaptor = ArgumentCaptor.forClass(List.class);
        verify(matchRepository).saveAll(matchCaptor.capture());
        List<ReconciliationMatch> savedMatches = matchCaptor.getValue();
        assertThat(savedMatches).hasSize(1);
        assertThat(savedMatches.get(0).getMatchType()).isEqualTo(MatchType.TOLERANCE);
        assertThat(savedMatches.get(0).getDateDifferenceDays()).isEqualTo(1);
    }

    @Test
    void executeReconciliation_amountMismatchDiscrepancy() {
        ExternalTransactionDto sourceTx = ExternalTransactionDto.builder()
                .id(103L)
                .transactionReference("TX-DISC-001")
                .amount(new BigDecimal("500.00"))
                .transactionDate(LocalDate.of(2026, 3, 1))
                .build();
        when(financialDataClient.getUnreconciledTransactions())
                .thenReturn(ApiResponse.ok(List.of(sourceTx), "Unreconciled list"));

        // Amount differs by $100.00, exceeding $0.05 tolerance
        ExternalTransactionDto targetTx = ExternalTransactionDto.builder()
                .id(203L)
                .transactionReference("TX-DISC-001")
                .amount(new BigDecimal("600.00"))
                .transactionDate(LocalDate.of(2026, 3, 1))
                .build();

        ExecuteReconciliationRequest request = ExecuteReconciliationRequest.builder()
                .amountTolerance(new BigDecimal("0.05"))
                .externalCounterpartTransactions(List.of(targetTx))
                .build();

        ReconciliationRunResponse response = reconciliationService.executeReconciliation(request, "test-user");

        assertThat(response).isNotNull();
        assertThat(response.getExactMatchesCount()).isEqualTo(0);
        assertThat(response.getDiscrepanciesCount()).isEqualTo(1);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ReconciliationDiscrepancy>> discCaptor = ArgumentCaptor.forClass(List.class);
        verify(discrepancyRepository).saveAll(discCaptor.capture());
        List<ReconciliationDiscrepancy> savedDisc = discCaptor.getValue();
        assertThat(savedDisc).hasSize(1);
        assertThat(savedDisc.get(0).getDiscrepancyType()).isEqualTo(DiscrepancyType.AMOUNT_MISMATCH);
    }
}
