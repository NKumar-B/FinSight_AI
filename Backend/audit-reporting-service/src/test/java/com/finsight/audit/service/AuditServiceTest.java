package com.finsight.audit.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finsight.audit.dto.response.AuditEventResponse;
import com.finsight.audit.dto.response.AuditSummaryStatsResponse;
import com.finsight.audit.entity.AuditEvent;
import com.finsight.audit.mapper.AuditMapper;
import com.finsight.audit.repository.AuditEventRepository;
import com.finsight.audit.service.impl.AuditServiceImpl;
import com.finsight.common.dto.AuditEventDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private AuditEventRepository auditRepository;

    @Spy
    private AuditMapper mapper = new AuditMapper();

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private AuditServiceImpl auditService;

    @Test
    void recordEvent_success() {
        AuditEventDto dto = AuditEventDto.builder()
                .actorId("user-admin")
                .actorType("USER")
                .action("ACCOUNT_CREATED")
                .entityType("ACCOUNT")
                .entityId("55")
                .correlationId("corr-12345")
                .outcome("SUCCESS")
                .metadata(Map.of("currency", "USD", "balance", 1000.0))
                .timestamp(Instant.now())
                .build();

        when(auditRepository.save(any(AuditEvent.class))).thenAnswer(i -> {
            AuditEvent ev = i.getArgument(0);
            ev.setId(1L);
            return ev;
        });

        AuditEventResponse response = auditService.recordEvent(dto);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getActorId()).isEqualTo("user-admin");
        assertThat(response.getAction()).isEqualTo("ACCOUNT_CREATED");
        assertThat(response.getEntityType()).isEqualTo("ACCOUNT");
        assertThat(response.getOutcome()).isEqualTo("SUCCESS");

        ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(auditRepository).save(captor.capture());
        AuditEvent saved = captor.getValue();
        assertThat(saved.getMetadata()).contains("currency");
    }

    @Test
    void getAuditStats_returnsAggregatedMetrics() {
        when(auditRepository.count()).thenReturn(100L);
        when(auditRepository.countByOutcome("SUCCESS")).thenReturn(95L);
        when(auditRepository.countByOutcome("FAILURE")).thenReturn(5L);
        when(auditRepository.countGroupedByActorType()).thenReturn(List.of(
                new Object[]{"USER", 60L},
                new Object[]{"SYSTEM", 40L}
        ));
        when(auditRepository.countTopActions(any())).thenReturn(List.of(
                new Object[]{"USER_LOGIN", 50L},
                new Object[]{"TRANSACTION_POST", 30L}
        ));

        AuditSummaryStatsResponse stats = auditService.getAuditStats();

        assertThat(stats).isNotNull();
        assertThat(stats.getTotalEvents()).isEqualTo(100L);
        assertThat(stats.getSuccessCount()).isEqualTo(95L);
        assertThat(stats.getFailureCount()).isEqualTo(5L);
        assertThat(stats.getEventsByActorType()).containsEntry("USER", 60L);
        assertThat(stats.getTopActions()).containsEntry("USER_LOGIN", 50L);
    }
}
