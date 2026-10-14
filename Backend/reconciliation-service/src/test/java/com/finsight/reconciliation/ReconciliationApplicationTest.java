package com.finsight.reconciliation;

import com.finsight.reconciliation.client.AuditClient;
import com.finsight.reconciliation.client.FinancialDataClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class ReconciliationApplicationTest {

    @MockBean
    private FinancialDataClient financialDataClient;

    @MockBean
    private AuditClient auditClient;

    @Test
    void contextLoads() {
    }
}
