package com.finsight.financial.service;

import com.finsight.common.dto.PagedResponse;
import com.finsight.financial.dto.request.CreateTransactionRequest;
import com.finsight.financial.dto.request.UpdateTransactionStatusRequest;
import com.finsight.financial.dto.response.TransactionResponse;
import com.finsight.financial.enums.TransactionStatus;
import com.finsight.financial.enums.TransactionType;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface TransactionService {
    TransactionResponse createTransaction(CreateTransactionRequest request);
    PagedResponse<TransactionResponse> searchTransactions(
            Long accountId,
            LocalDate startDate,
            LocalDate endDate,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            TransactionType type,
            TransactionStatus status,
            String category,
            String search,
            Pageable pageable);
    TransactionResponse getTransactionById(Long id);
    TransactionResponse getTransactionByReference(String reference);
    List<TransactionResponse> getUnreconciledTransactions();
    PagedResponse<TransactionResponse> getTransactionHistory(Long accountId, LocalDate startDate, LocalDate endDate, Pageable pageable);
    TransactionResponse updateTransactionStatus(Long id, UpdateTransactionStatusRequest request);
}
