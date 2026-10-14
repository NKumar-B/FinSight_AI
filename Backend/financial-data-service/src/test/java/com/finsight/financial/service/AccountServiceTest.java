package com.finsight.financial.service;

import com.finsight.common.exception.ConflictException;
import com.finsight.common.exception.ResourceNotFoundException;
import com.finsight.financial.client.AuditClient;
import com.finsight.financial.dto.request.CreateAccountRequest;
import com.finsight.financial.dto.response.AccountResponse;
import com.finsight.financial.entity.Account;
import com.finsight.financial.enums.AccountStatus;
import com.finsight.financial.enums.AccountType;
import com.finsight.financial.mapper.AccountMapper;
import com.finsight.financial.repository.AccountRepository;
import com.finsight.financial.service.impl.AccountServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private AuditClient auditClient;

    @InjectMocks
    private AccountServiceImpl accountService;

    private Account sampleAccount;

    @BeforeEach
    void setUp() {
        sampleAccount = Account.builder()
                .id(1L)
                .accountNumber("ACC-10001")
                .accountName("Corporate Operating")
                .accountType(AccountType.CHECKING)
                .currency("USD")
                .balance(new BigDecimal("50000.00"))
                .status(AccountStatus.ACTIVE)
                .build();
    }

    @Test
    void testCreateAccountSuccess() {
        CreateAccountRequest request = CreateAccountRequest.builder()
                .accountNumber("ACC-10001")
                .accountName("Corporate Operating")
                .accountType(AccountType.CHECKING)
                .currency("USD")
                .initialBalance(new BigDecimal("50000.00"))
                .build();

        when(accountRepository.existsByAccountNumber("ACC-10001")).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenReturn(sampleAccount);
        when(accountMapper.toResponse(sampleAccount)).thenReturn(AccountResponse.builder()
                .id(1L)
                .accountNumber("ACC-10001")
                .balance(new BigDecimal("50000.00"))
                .build());

        AccountResponse response = accountService.createAccount(request);
        assertNotNull(response);
        assertEquals("ACC-10001", response.getAccountNumber());
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    void testCreateAccountDuplicateNumberThrowsConflict() {
        CreateAccountRequest request = CreateAccountRequest.builder()
                .accountNumber("ACC-10001")
                .accountName("Corporate Operating")
                .accountType(AccountType.CHECKING)
                .currency("USD")
                .build();

        when(accountRepository.existsByAccountNumber("ACC-10001")).thenReturn(true);

        assertThrows(ConflictException.class, () -> accountService.createAccount(request));
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void testGetAccountByIdSuccess() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(sampleAccount));
        when(accountMapper.toResponse(sampleAccount)).thenReturn(AccountResponse.builder().id(1L).accountNumber("ACC-10001").build());

        AccountResponse response = accountService.getAccountById(1L);
        assertNotNull(response);
        assertEquals(1L, response.getId());
    }

    @Test
    void testGetAccountByIdNotFoundThrowsException() {
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> accountService.getAccountById(99L));
    }
}
