package com.finsight.financial.service.impl;

import com.finsight.common.context.CorrelationIdContext;
import com.finsight.common.dto.AuditEventDto;
import com.finsight.common.dto.PagedResponse;
import com.finsight.common.exception.ConflictException;
import com.finsight.common.exception.InvalidRequestException;
import com.finsight.common.exception.ResourceNotFoundException;
import com.finsight.financial.client.AuditClient;
import com.finsight.financial.dto.request.CreateTransactionRequest;
import com.finsight.financial.dto.request.UpdateTransactionStatusRequest;
import com.finsight.financial.dto.response.TransactionResponse;
import com.finsight.financial.entity.Account;
import com.finsight.financial.entity.Transaction;
import com.finsight.financial.enums.AccountStatus;
import com.finsight.financial.enums.TransactionStatus;
import com.finsight.financial.enums.TransactionType;
import com.finsight.financial.mapper.TransactionMapper;
import com.finsight.financial.repository.AccountRepository;
import com.finsight.financial.repository.TransactionRepository;
import com.finsight.financial.service.TransactionService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {
    private static final Logger log = LoggerFactory.getLogger(TransactionServiceImpl.class);

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final TransactionMapper transactionMapper;
    private final AuditClient auditClient;

    @Override
    @Transactional
    public TransactionResponse createTransaction(CreateTransactionRequest request) {
        log.info("[TransactionService] Creating transaction: accountId={} ref={} amount={}",
                request.getAccountId(), request.getTransactionReference(), request.getAmount());

        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", request.getAccountId()));

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new InvalidRequestException("Cannot post transaction to inactive/closed account: " + account.getAccountNumber());
        }

        BigDecimal normalizedAmount = request.getAmount().setScale(2, RoundingMode.HALF_UP);
        String currency = (request.getCurrency() != null && !request.getCurrency().trim().isEmpty())
                ? request.getCurrency().trim().toUpperCase()
                : account.getCurrency();

        String fingerprint = generateFingerprint(account.getId(), request.getTransactionReference(),
                request.getTransactionDate(), normalizedAmount, request.getTransactionType());

        if (transactionRepository.existsByFingerprint(fingerprint)) {
            throw new ConflictException("Duplicate transaction detected with identical details and reference: " + request.getTransactionReference());
        }

        Transaction transaction = Transaction.builder()
                .account(account)
                .transactionReference(request.getTransactionReference().trim())
                .transactionDate(request.getTransactionDate())
                .valueDate(request.getValueDate() != null ? request.getValueDate() : request.getTransactionDate())
                .amount(normalizedAmount)
                .currency(currency)
                .transactionType(request.getTransactionType())
                .description(request.getDescription())
                .category(request.getCategory())
                .merchantName(request.getMerchantName())
                .counterpartAccount(request.getCounterpartAccount())
                .status(TransactionStatus.POSTED)
                .fingerprint(fingerprint)
                .build();

        // Update Account Balance
        if (request.getTransactionType() == TransactionType.CREDIT) {
            account.setBalance(account.getBalance().add(normalizedAmount));
        } else {
            account.setBalance(account.getBalance().subtract(normalizedAmount));
        }
        accountRepository.save(account);

        Transaction saved = transactionRepository.save(transaction);

        sendAuditSafe("TRANSACTION_POSTED", "TRANSACTION", saved.getId().toString(), "SUCCESS",
                Map.of("reference", saved.getTransactionReference(), "amount", saved.getAmount(), "type", saved.getTransactionType().name()));

        return transactionMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<TransactionResponse> searchTransactions(
            Long accountId,
            LocalDate startDate,
            LocalDate endDate,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            TransactionType type,
            TransactionStatus status,
            String category,
            String search,
            Pageable pageable) {

        Specification<Transaction> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (accountId != null) {
                predicates.add(cb.equal(root.get("account").get("id"), accountId));
            }
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("transactionDate"), startDate));
            }
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("transactionDate"), endDate));
            }
            if (minAmount != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("amount"), minAmount));
            }
            if (maxAmount != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("amount"), maxAmount));
            }
            if (type != null) {
                predicates.add(cb.equal(root.get("transactionType"), type));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (category != null && !category.trim().isEmpty()) {
                predicates.add(cb.equal(cb.upper(root.get("category")), category.trim().toUpperCase()));
            }
            if (search != null && !search.trim().isEmpty()) {
                String searchPattern = "%" + search.trim().toUpperCase() + "%";
                Predicate refMatch = cb.like(cb.upper(root.get("transactionReference")), searchPattern);
                Predicate descMatch = cb.like(cb.upper(root.get("description")), searchPattern);
                Predicate merchantMatch = cb.like(cb.upper(root.get("merchantName")), searchPattern);
                predicates.add(cb.or(refMatch, descMatch, merchantMatch));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Transaction> page = transactionRepository.findAll(spec, pageable);
        List<TransactionResponse> content = page.getContent().stream()
                .map(transactionMapper::toResponse)
                .toList();

        return PagedResponse.<TransactionResponse>builder()
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
    public TransactionResponse getTransactionById(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", id));
        return transactionMapper.toResponse(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse getTransactionByReference(String reference) {
        Transaction transaction = transactionRepository.findByTransactionReference(reference.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "reference", reference));
        return transactionMapper.toResponse(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getUnreconciledTransactions() {
        return transactionRepository.findByStatus(TransactionStatus.POSTED).stream()
                .map(transactionMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<TransactionResponse> getTransactionHistory(
            Long accountId, LocalDate startDate, LocalDate endDate, Pageable pageable) {

        Page<Transaction> page;
        if (startDate != null && endDate != null) {
            page = transactionRepository.findByAccountIdAndTransactionDateBetween(accountId, startDate, endDate, pageable);
        } else {
            Specification<Transaction> spec = (root, query, cb) -> cb.equal(root.get("account").get("id"), accountId);
            page = transactionRepository.findAll(spec, pageable);
        }

        List<TransactionResponse> content = page.getContent().stream()
                .map(transactionMapper::toResponse)
                .toList();

        return PagedResponse.<TransactionResponse>builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    @Override
    @Transactional
    public TransactionResponse updateTransactionStatus(Long id, UpdateTransactionStatusRequest request) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", id));

        TransactionStatus oldStatus = transaction.getStatus();
        transaction.setStatus(request.getStatus());
        Transaction updated = transactionRepository.save(transaction);

        log.info("[TransactionService] Updated txn id={} status from {} to {}", id, oldStatus, request.getStatus());

        sendAuditSafe("TRANSACTION_STATUS_UPDATED", "TRANSACTION", updated.getId().toString(), "SUCCESS",
                Map.of("oldStatus", oldStatus.name(), "newStatus", request.getStatus().name(), "reason", request.getReason() != null ? request.getReason() : ""));

        return transactionMapper.toResponse(updated);
    }

    public static String generateFingerprint(Long accountId, String reference, LocalDate date, BigDecimal amount, TransactionType type) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String raw = String.format("%d:%s:%s:%s:%s",
                    accountId,
                    reference != null ? reference.trim().toUpperCase() : "",
                    date != null ? date.toString() : "",
                    amount != null ? amount.setScale(2, RoundingMode.HALF_UP).toPlainString() : "",
                    type != null ? type.name() : "");
            byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm unavailable", e);
        }
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
            log.warn("[TransactionService] Audit dispatch error: {}", e.getMessage());
        }
    }
}
