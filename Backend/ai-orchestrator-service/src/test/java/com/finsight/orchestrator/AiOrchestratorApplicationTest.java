package com.finsight.orchestrator;

import com.finsight.orchestrator.client.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class AiOrchestratorApplicationTest {

    @MockBean
    private FinancialDataClient financialDataClient;

    @MockBean
    private ReconciliationClient reconciliationClient;

    @MockBean
    private FraudDetectionClient fraudDetectionClient;

    @MockBean
    private FinancialInsightsClient financialInsightsClient;

    @MockBean
    private AuditClient auditClient;

    @Test
    void contextLoads() {
    }
}
