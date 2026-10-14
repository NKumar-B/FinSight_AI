package com.finsight.financial.service.impl;

import com.finsight.common.context.CorrelationIdContext;
import com.finsight.common.dto.AuditEventDto;
import com.finsight.common.dto.PagedResponse;
import com.finsight.common.exception.ConflictException;
import com.finsight.common.exception.InvalidRequestException;
import com.finsight.common.exception.ResourceNotFoundException;
import com.finsight.financial.client.AuditClient;
import com.finsight.financial.dto.response.CsvImportResultResponse;
import com.finsight.financial.dto.response.ImportBatchResponse;
import com.finsight.financial.dto.response.TransactionResponse;
import com.finsight.financial.entity.Account;
import com.finsight.financial.entity.ImportBatch;
import com.finsight.financial.entity.Transaction;
import com.finsight.financial.enums.AccountStatus;
import com.finsight.financial.enums.ImportBatchStatus;
import com.finsight.financial.enums.TransactionStatus;
import com.finsight.financial.enums.TransactionType;
import com.finsight.financial.mapper.ImportBatchMapper;
import com.finsight.financial.mapper.TransactionMapper;
import com.finsight.financial.repository.AccountRepository;
import com.finsight.financial.repository.ImportBatchRepository;
import com.finsight.financial.repository.TransactionRepository;
import com.finsight.financial.service.FinancialImportService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class FinancialImportServiceImpl implements FinancialImportService {
    private static final Logger log = LoggerFactory.getLogger(FinancialImportServiceImpl.class);

    private static final List<DateTimeFormatter> DATE_FORMATTERS = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE,                     // yyyy-MM-dd
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy")
    );

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final ImportBatchRepository importBatchRepository;
    private final ImportBatchMapper importBatchMapper;
    private final TransactionMapper transactionMapper;
    private final AuditClient auditClient;

    @Override
    @Transactional
    public CsvImportResultResponse importCsv(MultipartFile file, Long defaultAccountId, String importedBy) {
        if (file == null || file.isEmpty()) {
            throw new InvalidRequestException("Uploaded CSV file cannot be empty");
        }

        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "records.csv";
        log.info("[ImportService] Initiating CSV import: file={} defaultAccountId={}", originalFilename, defaultAccountId);

        // Calculate SHA-256 Checksum for Idempotency
        byte[] fileBytes;
        try {
            fileBytes = file.getBytes();
        } catch (Exception e) {
            throw new InvalidRequestException("Failed to read uploaded file contents: " + e.getMessage());
        }
        String fileChecksum = calculateSha256(fileBytes);

        // Check if file was already successfully imported
        Optional<ImportBatch> existingBatch = importBatchRepository.findByFileChecksumSha256(fileChecksum);
        if (existingBatch.isPresent() && existingBatch.get().getStatus() == ImportBatchStatus.COMPLETED) {
            log.warn("[ImportService] Duplicate file checksum detected: {}", fileChecksum);
            throw new ConflictException("Duplicate import: identical CSV has already been processed under batch: "
                    + existingBatch.get().getBatchReference());
        }

        String batchReference = "BATCH-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        ImportBatch batch = ImportBatch.builder()
                .batchReference(batchReference)
                .fileName(originalFilename)
                .fileChecksumSha256(fileChecksum)
                .accountId(defaultAccountId)
                .status(ImportBatchStatus.PROCESSING)
                .importedBy(importedBy != null ? importedBy : "SYSTEM")
                .build();
        batch = importBatchRepository.save(batch);

        List<String> errorMessages = new ArrayList<>();
        List<Transaction> savedTransactions = new ArrayList<>();
        Map<Long, Account> accountCache = new HashMap<>();

        if (defaultAccountId != null) {
            accountRepository.findById(defaultAccountId).ifPresent(acc -> accountCache.put(acc.getId(), acc));
        }

        int totalRecords = 0;
        int successfulRecords = 0;
        int failedRecords = 0;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser csvParser = CSVFormat.DEFAULT
                     .builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setIgnoreHeaderCase(true)
                     .setTrim(true)
                     .build()
                     .parse(reader)) {

            for (CSVRecord record : csvParser) {
                totalRecords++;
                long rowNumber = record.getRecordNumber();

                try {
                    // 1. Resolve Account
                    Account account = resolveAccount(record, defaultAccountId, accountCache);
                    if (account == null) {
                        throw new IllegalArgumentException("Unable to resolve valid active account for row");
                    }

                    // 2. Resolve Transaction Reference
                    String ref = getColumnValue(record, "transactionReference", "reference", "ref");
                    if (ref == null || ref.trim().isEmpty()) {
                        ref = "TXN-" + batchReference + "-" + String.format("%04d", rowNumber);
                    }

                    // 3. Resolve & Normalize Dates
                    String rawDate = getColumnValue(record, "transactionDate", "date", "txndate");
                    if (rawDate == null || rawDate.trim().isEmpty()) {
                        throw new IllegalArgumentException("Transaction date is required");
                    }
                    LocalDate txnDate = parseDate(rawDate);

                    String rawValueDate = getColumnValue(record, "valueDate", "valdate");
                    LocalDate valDate = (rawValueDate != null && !rawValueDate.trim().isEmpty())
                            ? parseDate(rawValueDate)
                            : txnDate;

                    // 4. Resolve & Normalize Amount and Type
                    String rawAmount = getColumnValue(record, "amount", "amt");
                    if (rawAmount == null || rawAmount.trim().isEmpty()) {
                        throw new IllegalArgumentException("Transaction amount is required");
                    }
                    BigDecimal parsedAmount = parseAmount(rawAmount);

                    String rawType = getColumnValue(record, "type", "transactionType", "dr_cr");
                    TransactionType txnType = resolveTransactionType(rawType, parsedAmount);
                    BigDecimal normalizedAmount = parsedAmount.abs().setScale(2, RoundingMode.HALF_UP);

                    // 5. Currency
                    String rawCurrency = getColumnValue(record, "currency", "curr");
                    String currency = (rawCurrency != null && !rawCurrency.trim().isEmpty())
                            ? rawCurrency.trim().toUpperCase()
                            : account.getCurrency();

                    // 6. Metadata
                    String description = getColumnValue(record, "description", "desc", "narrative");
                    String category = getColumnValue(record, "category", "cat");
                    String merchant = getColumnValue(record, "merchantName", "merchant", "payee");
                    String counterpart = getColumnValue(record, "counterpartAccount", "counterpart", "recipient");

                    // 7. Duplicate Fingerprint Check
                    String fingerprint = TransactionServiceImpl.generateFingerprint(
                            account.getId(), ref, txnDate, normalizedAmount, txnType);

                    if (transactionRepository.existsByFingerprint(fingerprint)) {
                        throw new IllegalArgumentException("Duplicate transaction with reference: " + ref);
                    }

                    // 8. Build Transaction
                    Transaction txn = Transaction.builder()
                            .account(account)
                            .transactionReference(ref)
                            .transactionDate(txnDate)
                            .valueDate(valDate)
                            .amount(normalizedAmount)
                            .currency(currency)
                            .transactionType(txnType)
                            .description(description)
                            .category(category)
                            .merchantName(merchant)
                            .counterpartAccount(counterpart)
                            .status(TransactionStatus.POSTED)
                            .importBatchId(batch.getId())
                            .fingerprint(fingerprint)
                            .build();

                    // Update Balance
                    if (txnType == TransactionType.CREDIT) {
                        account.setBalance(account.getBalance().add(normalizedAmount));
                    } else {
                        account.setBalance(account.getBalance().subtract(normalizedAmount));
                    }

                    savedTransactions.add(txn);
                    successfulRecords++;

                } catch (Exception rowEx) {
                    failedRecords++;
                    String errMsg = String.format("Row %d failed: %s", rowNumber, rowEx.getMessage());
                    errorMessages.add(errMsg);
                    log.warn("[ImportService] {}", errMsg);
                }
            }

            // Persist valid records & accounts
            if (!savedTransactions.isEmpty()) {
                accountRepository.saveAll(accountCache.values());
                transactionRepository.saveAll(savedTransactions);
            }

            // Update Batch Status
            batch.setTotalRecords(totalRecords);
            batch.setSuccessfulRecords(successfulRecords);
            batch.setFailedRecords(failedRecords);
            batch.setCompletedAt(Instant.now());

            if (failedRecords == 0 && successfulRecords > 0) {
                batch.setStatus(ImportBatchStatus.COMPLETED);
            } else if (successfulRecords > 0) {
                batch.setStatus(ImportBatchStatus.COMPLETED_WITH_ERRORS);
            } else {
                batch.setStatus(ImportBatchStatus.FAILED);
            }

            if (!errorMessages.isEmpty()) {
                batch.setErrorDetails(String.join("\n", errorMessages.subList(0, Math.min(errorMessages.size(), 50))));
            }

            importBatchRepository.save(batch);

            sendAuditSafe("CSV_IMPORTED", "IMPORT_BATCH",
                    batch.getId() != null ? batch.getId().toString() : batch.getBatchReference(),
                    batch.getStatus().name(),
                    Map.of("batchReference", batchReference, "total", totalRecords, "success", successfulRecords, "failed", failedRecords));

        } catch (Exception ex) {
            log.error("[ImportService] Catastrophic failure parsing CSV batch: {}", batchReference, ex);
            batch.setStatus(ImportBatchStatus.FAILED);
            batch.setErrorDetails("CSV Parsing failure: " + ex.getMessage());
            batch.setCompletedAt(Instant.now());
            importBatchRepository.save(batch);
            throw new InvalidRequestException("Failed to process CSV file: " + ex.getMessage());
        }

        List<TransactionResponse> txResponses = savedTransactions.stream()
                .map(transactionMapper::toResponse)
                .toList();

        return CsvImportResultResponse.builder()
                .batchReference(batch.getBatchReference())
                .fileName(batch.getFileName())
                .status(batch.getStatus())
                .totalRecords(totalRecords)
                .successfulRecords(successfulRecords)
                .failedRecords(failedRecords)
                .errorMessages(errorMessages)
                .importedTransactions(txResponses)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ImportBatchResponse> listBatches(Pageable pageable) {
        Page<ImportBatch> page = importBatchRepository.findAll(pageable);
        List<ImportBatchResponse> content = page.getContent().stream()
                .map(importBatchMapper::toResponse)
                .toList();

        return PagedResponse.<ImportBatchResponse>builder()
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
    public ImportBatchResponse getBatchById(Long id) {
        ImportBatch batch = importBatchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ImportBatch", "id", id));
        return importBatchMapper.toResponse(batch);
    }

    private Account resolveAccount(CSVRecord record, Long defaultAccountId, Map<Long, Account> accountCache) {
        String accNum = getColumnValue(record, "accountNumber", "account", "account_no");
        if (accNum != null && !accNum.trim().isEmpty()) {
            return accountRepository.findByAccountNumber(accNum.trim().toUpperCase())
                    .map(acc -> {
                        accountCache.put(acc.getId(), acc);
                        return acc;
                    })
                    .orElse(null);
        }
        if (defaultAccountId != null) {
            return accountCache.computeIfAbsent(defaultAccountId, id -> accountRepository.findById(id).orElse(null));
        }
        return null;
    }

    private String getColumnValue(CSVRecord record, String... possibleNames) {
        for (String name : possibleNames) {
            if (record.isMapped(name)) {
                String val = record.get(name);
                if (val != null && !val.trim().isEmpty()) {
                    return val.trim();
                }
            }
        }
        return null;
    }

    private LocalDate parseDate(String raw) {
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                return LocalDate.parse(raw.trim(), formatter);
            } catch (DateTimeParseException ignored) {
            }
        }
        throw new IllegalArgumentException("Unparseable date: '" + raw + "'. Supported formats: yyyy-MM-dd, dd/MM/yyyy, MM/dd/yyyy");
    }

    private BigDecimal parseAmount(String raw) {
        String cleaned = raw.replaceAll("[$,€£\\s]", "").trim();
        try {
            return new BigDecimal(cleaned);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid decimal amount: '" + raw + "'");
        }
    }

    private TransactionType resolveTransactionType(String rawType, BigDecimal amount) {
        if (rawType != null) {
            String upper = rawType.trim().toUpperCase();
            if (upper.equals("CR") || upper.equals("CREDIT") || upper.equals("DEPOSIT")) {
                return TransactionType.CREDIT;
            }
            if (upper.equals("DR") || upper.equals("DEBIT") || upper.equals("WITHDRAWAL")) {
                return TransactionType.DEBIT;
            }
        }
        return amount.compareTo(BigDecimal.ZERO) < 0 ? TransactionType.DEBIT : TransactionType.CREDIT;
    }

    private String calculateSha256(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm missing", e);
        }
    }

    private void sendAuditSafe(String action, String entityType, String entityId, String outcome, Map<String, Object> metadata) {
        try {
            AuditEventDto audit = AuditEventDto.builder()
                    .actorId("IMPORT_SERVICE")
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
            log.warn("[ImportService] Audit dispatch error: {}", e.getMessage());
        }
    }
}
