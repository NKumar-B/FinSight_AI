package com.finsight.fraud;

import com.finsight.fraud.client.AuditClient;
import com.finsight.fraud.client.FinancialDataClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class FraudDetectionApplicationTest {

    @MockBean
    private FinancialDataClient financialDataClient;

    @MockBean
    private AuditClient auditClient;

    @Test
    void contextLoads() {
    }
}
