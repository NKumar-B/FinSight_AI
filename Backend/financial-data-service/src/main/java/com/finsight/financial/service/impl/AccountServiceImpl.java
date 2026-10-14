package com.finsight.financial.service.impl;

import com.finsight.common.context.CorrelationIdContext;
import com.finsight.common.dto.AuditEventDto;
import com.finsight.common.dto.PagedResponse;
import com.finsight.common.exception.ConflictException;
import com.finsight.common.exception.ResourceNotFoundException;
import com.finsight.financial.client.AuditClient;
import com.finsight.financial.dto.request.CreateAccountRequest;
import com.finsight.financial.dto.response.AccountResponse;
import com.finsight.financial.entity.Account;
import com.finsight.financial.enums.AccountStatus;
import com.finsight.financial.mapper.AccountMapper;
import com.finsight.financial.repository.AccountRepository;
import com.finsight.financial.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
    private static final Logger log = LoggerFactory.getLogger(AccountServiceImpl.class);

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final AuditClient auditClient;

    @Override
    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request) {
        log.info("[AccountService] Creating account: number={} name={} currency={}",
                request.getAccountNumber(), request.getAccountName(), request.getCurrency());

        if (accountRepository.existsByAccountNumber(request.getAccountNumber().trim())) {
            throw new ConflictException("Account already exists with number: " + request.getAccountNumber());
        }

        BigDecimal initialBal = request.getInitialBalance() != null ? request.getInitialBalance() : BigDecimal.ZERO;

        Account account = Account.builder()
                .accountNumber(request.getAccountNumber().trim().toUpperCase())
                .accountName(request.getAccountName().trim())
                .accountType(request.getAccountType())
                .currency(request.getCurrency().trim().toUpperCase())
                .balance(initialBal)
                .status(AccountStatus.ACTIVE)
                .institutionName(request.getInstitutionName())
                .build();

        Account saved = accountRepository.save(account);

        sendAuditSafe("ACCOUNT_CREATED", "ACCOUNT", saved.getId().toString(), "SUCCESS",
                Map.of("accountNumber", saved.getAccountNumber(), "currency", saved.getCurrency(), "balance", saved.getBalance()));

        return accountMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<AccountResponse> listAccounts(String search, Pageable pageable) {
        Page<Account> page;
        if (search != null && !search.trim().isEmpty()) {
            page = accountRepository.findByAccountNameContainingIgnoreCaseOrAccountNumberContainingIgnoreCase(
                    search.trim(), search.trim(), pageable);
        } else {
            page = accountRepository.findAll(pageable);
        }

        List<AccountResponse> content = page.getContent().stream()
                .map(accountMapper::toResponse)
                .toList();

        return PagedResponse.<AccountResponse>builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponse getAccountById(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", id));
        return accountMapper.toResponse(account);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponse getAccountByNumber(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber.trim().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Account", "accountNumber", accountNumber));
        return accountMapper.toResponse(account);
    }

    private void sendAuditSafe(String action, String entityType, String entityId, String outcome, Map<String, Object> metadata) {
        try {
            AuditEventDto audit = AuditEventDto.builder()
                    .actorId("FINANCIAL_SERVICE")
                    .actorType("SYSTEM")
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .correlationId(CorrelationIdContext.get())
                    .outcome(outcome)
                    .metadata(metadata)
                    .timestamp(Instant.now())
                    .build();
            auditClient.recordAuditEvent(audit);
        } catch (Exception e) {
            log.warn("[AccountService] Failed to dispatch audit event: {}", e.getMessage());
        }
    }
}
