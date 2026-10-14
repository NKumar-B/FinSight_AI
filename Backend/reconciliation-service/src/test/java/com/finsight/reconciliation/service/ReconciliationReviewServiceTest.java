package com.finsight.reconciliation.service;

import com.finsight.reconciliation.client.AuditClient;
import com.finsight.reconciliation.dto.request.ReviewDecisionRequest;
import com.finsight.reconciliation.dto.response.ReviewDecisionResponse;
import com.finsight.reconciliation.entity.ReconciliationDiscrepancy;
import com.finsight.reconciliation.entity.ReviewDecision;
import com.finsight.reconciliation.enums.CaseStatus;
import com.finsight.reconciliation.enums.DecisionType;
import com.finsight.reconciliation.enums.DiscrepancyType;
import com.finsight.reconciliation.mapper.ReconciliationMapper;
import com.finsight.reconciliation.repository.ReconciliationDiscrepancyRepository;
import com.finsight.reconciliation.repository.ReviewDecisionRepository;
import com.finsight.reconciliation.service.impl.ReconciliationReviewServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReconciliationReviewServiceTest {

    @Mock
    private ReconciliationDiscrepancyRepository discrepancyRepository;

    @Mock
    private ReviewDecisionRepository decisionRepository;

    @Mock
    private AuditClient auditClient;

    @Spy
    private ReconciliationMapper mapper = new ReconciliationMapper();

    @InjectMocks
    private ReconciliationReviewServiceImpl reviewService;

    @Test
    void submitDecision_approveMatch_success() {
        ReconciliationDiscrepancy discrepancy = ReconciliationDiscrepancy.builder()
                .id(1L)
                .caseReference("DISC-1001")
                .discrepancyType(DiscrepancyType.AMOUNT_MISMATCH)
                .sourceAmount(new BigDecimal("100.00"))
                .targetAmount(new BigDecimal("99.98"))
                .sourceDate(LocalDate.of(2026, 1, 1))
                .status(CaseStatus.OPEN)
                .build();

        when(discrepancyRepository.findById(1L)).thenReturn(Optional.of(discrepancy));
        when(discrepancyRepository.save(any(ReconciliationDiscrepancy.class))).thenAnswer(i -> i.getArgument(0));
        when(decisionRepository.save(any(ReviewDecision.class))).thenAnswer(i -> {
            ReviewDecision d = i.getArgument(0);
            d.setId(10L);
            return d;
        });

        ReviewDecisionRequest request = ReviewDecisionRequest.builder()
                .decision(DecisionType.APPROVE_MATCH)
                .reason("Minor 2 cent rounding difference is approved")
                .build();

        ReviewDecisionResponse response = reviewService.submitDecision(1L, request, "analyst_jane");

        assertThat(response).isNotNull();
        assertThat(response.getDecision()).isEqualTo(DecisionType.APPROVE_MATCH);
        assertThat(response.getReviewerId()).isEqualTo("analyst_jane");
        assertThat(discrepancy.getStatus()).isEqualTo(CaseStatus.RESOLVED_APPROVED);
        assertThat(discrepancy.getAssignedTo()).isEqualTo("analyst_jane");

        verify(discrepancyRepository).save(discrepancy);
        verify(decisionRepository).save(any(ReviewDecision.class));
    }

    @Test
    void submitDecision_writeOff_success() {
        ReconciliationDiscrepancy discrepancy = ReconciliationDiscrepancy.builder()
                .id(2L)
                .caseReference("DISC-1002")
                .discrepancyType(DiscrepancyType.UNMATCHED_SOURCE)
                .sourceAmount(new BigDecimal("5.00"))
                .status(CaseStatus.OPEN)
                .build();

        when(discrepancyRepository.findById(2L)).thenReturn(Optional.of(discrepancy));
        when(discrepancyRepository.save(any(ReconciliationDiscrepancy.class))).thenAnswer(i -> i.getArgument(0));
        when(decisionRepository.save(any(ReviewDecision.class))).thenAnswer(i -> {
            ReviewDecision d = i.getArgument(0);
            d.setId(11L);
            return d;
        });

        ReviewDecisionRequest request = ReviewDecisionRequest.builder()
                .decision(DecisionType.WRITE_OFF)
                .reason("Small legacy fee written off per policy")
                .build();

        ReviewDecisionResponse response = reviewService.submitDecision(2L, request, "manager_bob");

        assertThat(response).isNotNull();
        assertThat(response.getDecision()).isEqualTo(DecisionType.WRITE_OFF);
        assertThat(discrepancy.getStatus()).isEqualTo(CaseStatus.RESOLVED_APPROVED);
    }
}
