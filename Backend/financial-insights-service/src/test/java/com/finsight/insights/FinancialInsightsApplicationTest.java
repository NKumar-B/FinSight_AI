package com.finsight.insights;

import com.finsight.insights.client.AuditClient;
import com.finsight.insights.client.FinancialDataClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class FinancialInsightsApplicationTest {

    @MockBean
    private FinancialDataClient financialDataClient;

    @MockBean
    private AuditClient auditClient;

    @Test
    void contextLoads() {
    }
}
