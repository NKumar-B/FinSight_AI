package com.finsight.financial.service;

import com.finsight.common.exception.ConflictException;
import com.finsight.common.exception.InvalidRequestException;
import com.finsight.financial.client.AuditClient;
import com.finsight.financial.dto.response.CsvImportResultResponse;
import com.finsight.financial.entity.Account;
import com.finsight.financial.entity.ImportBatch;
import com.finsight.financial.enums.AccountStatus;
import com.finsight.financial.enums.AccountType;
import com.finsight.financial.enums.ImportBatchStatus;
import com.finsight.financial.mapper.ImportBatchMapper;
import com.finsight.financial.mapper.TransactionMapper;
import com.finsight.financial.repository.AccountRepository;
import com.finsight.financial.repository.ImportBatchRepository;
import com.finsight.financial.repository.TransactionRepository;
import com.finsight.financial.service.impl.FinancialImportServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FinancialImportServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private ImportBatchRepository importBatchRepository;

    @Mock
    private ImportBatchMapper importBatchMapper;

    @Mock
    private TransactionMapper transactionMapper;

    @Mock
    private AuditClient auditClient;

    @InjectMocks
    private FinancialImportServiceImpl importService;

    private Account sampleAccount;

    @BeforeEach
    void setUp() {
        sampleAccount = Account.builder()
                .id(1L)
                .accountNumber("ACC-10001")
                .accountName("Corporate Account")
                .accountType(AccountType.CHECKING)
                .currency("USD")
                .balance(new BigDecimal("10000.00"))
                .status(AccountStatus.ACTIVE)
                .build();
    }

    @Test
    void testImportCsvSuccessWithNormalization() {
        String csvContent = "accountNumber,reference,date,amount,type,description,category,merchant\n" +
                "ACC-10001,TXN-CSV-01,2026-10-10,$1500.50,CR,Client Payment,SALES,Acme Corp\n" +
                "ACC-10001,TXN-CSV-02,11/10/2026,300.00,DR,SaaS Subscription,EXPENSE,AWS\n";

        MockMultipartFile file = new MockMultipartFile(
                "file", "statement.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8));

        when(importBatchRepository.save(any(ImportBatch.class))).thenAnswer(i -> {
            ImportBatch b = i.getArgument(0);
            if (b.getId() == null) {
                b.setId(100L);
            }
            return b;
        });
        when(accountRepository.findByAccountNumber("ACC-10001")).thenReturn(Optional.of(sampleAccount));
        when(transactionRepository.existsByFingerprint(any())).thenReturn(false);

        CsvImportResultResponse result = importService.importCsv(file, 1L, "analyst1");

        assertNotNull(result);
        assertEquals(2, result.getTotalRecords());
        assertEquals(2, result.getSuccessfulRecords());
        assertEquals(0, result.getFailedRecords());
        assertEquals(ImportBatchStatus.COMPLETED, result.getStatus());

        verify(transactionRepository, times(1)).saveAll(any());
        verify(accountRepository, times(1)).saveAll(any());
    }

    @Test
    void testImportCsvDuplicateChecksumThrowsConflict() {
        String csvContent = "accountNumber,reference,date,amount,type\nACC-10001,TXN-01,2026-10-10,100,CR\n";
        MockMultipartFile file = new MockMultipartFile(
                "file", "duplicate.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8));

        ImportBatch existingBatch = ImportBatch.builder()
                .batchReference("BATCH-PREVIOUS")
                .status(ImportBatchStatus.COMPLETED)
                .build();

        when(importBatchRepository.findByFileChecksumSha256(any())).thenReturn(Optional.of(existingBatch));

        assertThrows(ConflictException.class, () -> importService.importCsv(file, 1L, "analyst1"));
        verify(transactionRepository, never()).saveAll(any());
    }

    @Test
    void testImportEmptyFileThrowsInvalidRequest() {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "empty.csv", "text/csv", new byte[0]);

        assertThrows(InvalidRequestException.class, () -> importService.importCsv(emptyFile, 1L, "analyst1"));
    }
}
