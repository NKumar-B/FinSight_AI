package com.finsight.financial.service;

import com.finsight.common.exception.ConflictException;
import com.finsight.common.exception.ResourceNotFoundException;
import com.finsight.financial.client.AuditClient;
import com.finsight.financial.dto.request.CreateTransactionRequest;
import com.finsight.financial.dto.request.UpdateTransactionStatusRequest;
import com.finsight.financial.dto.response.TransactionResponse;
import com.finsight.financial.entity.Account;
import com.finsight.financial.entity.Transaction;
import com.finsight.financial.enums.AccountStatus;
import com.finsight.financial.enums.AccountType;
import com.finsight.financial.enums.TransactionStatus;
import com.finsight.financial.enums.TransactionType;
import com.finsight.financial.mapper.TransactionMapper;
import com.finsight.financial.repository.AccountRepository;
import com.finsight.financial.repository.TransactionRepository;
import com.finsight.financial.service.impl.TransactionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionMapper transactionMapper;

    @Mock
    private AuditClient auditClient;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private Account sampleAccount;
    private Transaction sampleTransaction;

    @BeforeEach
    void setUp() {
        sampleAccount = Account.builder()
                .id(1L)
                .accountNumber("ACC-10001")
                .accountName("Main Account")
                .accountType(AccountType.CHECKING)
                .currency("USD")
                .balance(new BigDecimal("1000.00"))
                .status(AccountStatus.ACTIVE)
                .build();

        sampleTransaction = Transaction.builder()
                .id(10L)
                .account(sampleAccount)
                .transactionReference("TXN-001")
                .transactionDate(LocalDate.now())
                .amount(new BigDecimal("250.00"))
                .currency("USD")
                .transactionType(TransactionType.CREDIT)
                .status(TransactionStatus.POSTED)
                .build();
    }

    @Test
    void testCreateTransactionCreditIncreasesBalance() {
        CreateTransactionRequest request = CreateTransactionRequest.builder()
                .accountId(1L)
                .transactionReference("TXN-001")
                .transactionDate(LocalDate.now())
                .amount(new BigDecimal("250.00"))
                .transactionType(TransactionType.CREDIT)
                .build();

        when(accountRepository.findById(1L)).thenReturn(Optional.of(sampleAccount));
        when(transactionRepository.existsByFingerprint(any())).thenReturn(false);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(sampleTransaction);
        when(transactionMapper.toResponse(sampleTransaction)).thenReturn(TransactionResponse.builder()
                .id(10L)
                .amount(new BigDecimal("250.00"))
                .transactionType(TransactionType.CREDIT)
                .build());

        TransactionResponse response = transactionService.createTransaction(request);
        assertNotNull(response);
        assertEquals(new BigDecimal("1250.00"), sampleAccount.getBalance());
        verify(accountRepository, times(1)).save(sampleAccount);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void testCreateTransactionDebitDecreasesBalance() {
        CreateTransactionRequest request = CreateTransactionRequest.builder()
                .accountId(1L)
                .transactionReference("TXN-002")
                .transactionDate(LocalDate.now())
                .amount(new BigDecimal("400.00"))
                .transactionType(TransactionType.DEBIT)
                .build();

        when(accountRepository.findById(1L)).thenReturn(Optional.of(sampleAccount));
        when(transactionRepository.existsByFingerprint(any())).thenReturn(false);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(sampleTransaction);
        when(transactionMapper.toResponse(sampleTransaction)).thenReturn(TransactionResponse.builder().id(11L).build());

        transactionService.createTransaction(request);
        assertEquals(new BigDecimal("600.00"), sampleAccount.getBalance());
    }

    @Test
    void testDuplicateFingerprintThrowsConflict() {
        CreateTransactionRequest request = CreateTransactionRequest.builder()
                .accountId(1L)
                .transactionReference("TXN-001")
                .transactionDate(LocalDate.now())
                .amount(new BigDecimal("250.00"))
                .transactionType(TransactionType.CREDIT)
                .build();

        when(accountRepository.findById(1L)).thenReturn(Optional.of(sampleAccount));
        when(transactionRepository.existsByFingerprint(any())).thenReturn(true);

        assertThrows(ConflictException.class, () -> transactionService.createTransaction(request));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void testUpdateTransactionStatusSuccess() {
        UpdateTransactionStatusRequest request = UpdateTransactionStatusRequest.builder()
                .status(TransactionStatus.RECONCILED)
                .reason("Matched by reconciliation engine")
                .build();

        when(transactionRepository.findById(10L)).thenReturn(Optional.of(sampleTransaction));
        when(transactionRepository.save(sampleTransaction)).thenReturn(sampleTransaction);
        when(transactionMapper.toResponse(sampleTransaction)).thenReturn(TransactionResponse.builder()
                .id(10L)
                .status(TransactionStatus.RECONCILED)
                .build());

        TransactionResponse response = transactionService.updateTransactionStatus(10L, request);
        assertNotNull(response);
        assertEquals(TransactionStatus.RECONCILED, sampleTransaction.getStatus());
    }
}
