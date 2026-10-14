package com.finsight.financial.mapper;

import com.finsight.financial.dto.response.ImportBatchResponse;
import com.finsight.financial.entity.ImportBatch;
import org.springframework.stereotype.Component;

@Component
public class ImportBatchMapper {

    public ImportBatchResponse toResponse(ImportBatch batch) {
        if (batch == null) {
            return null;
        }

        return ImportBatchResponse.builder()
                .id(batch.getId())
                .batchReference(batch.getBatchReference())
                .fileName(batch.getFileName())
                .fileChecksumSha256(batch.getFileChecksumSha256())
                .accountId(batch.getAccountId())
                .status(batch.getStatus())
                .totalRecords(batch.getTotalRecords())
                .successfulRecords(batch.getSuccessfulRecords())
                .failedRecords(batch.getFailedRecords())
                .errorDetails(batch.getErrorDetails())
                .importedBy(batch.getImportedBy())
                .createdAt(batch.getCreatedAt())
                .completedAt(batch.getCompletedAt())
                .build();
    }
}
