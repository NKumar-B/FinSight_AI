package com.finsight.financial.mapper;

import com.finsight.financial.dto.response.TransactionResponse;
import com.finsight.financial.entity.Transaction;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public TransactionResponse toResponse(Transaction transaction) {
        if (transaction == null) {
            return null;
        }

        return TransactionResponse.builder()
                .id(transaction.getId())
                .accountId(transaction.getAccount() != null ? transaction.getAccount().getId() : null)
                .accountNumber(transaction.getAccount() != null ? transaction.getAccount().getAccountNumber() : null)
                .transactionReference(transaction.getTransactionReference())
                .transactionDate(transaction.getTransactionDate())
                .valueDate(transaction.getValueDate())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .transactionType(transaction.getTransactionType())
                .description(transaction.getDescription())
                .category(transaction.getCategory())
                .merchantName(transaction.getMerchantName())
                .counterpartAccount(transaction.getCounterpartAccount())
                .status(transaction.getStatus())
                .importBatchId(transaction.getImportBatchId())
                .createdAt(transaction.getCreatedAt())
                .updatedAt(transaction.getUpdatedAt())
                .build();
    }
}
