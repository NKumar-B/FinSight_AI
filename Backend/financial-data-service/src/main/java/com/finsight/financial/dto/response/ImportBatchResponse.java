package com.finsight.financial.dto.response;

import com.finsight.financial.enums.ImportBatchStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportBatchResponse {
    private Long id;
    private String batchReference;
    private String fileName;
    private String fileChecksumSha256;
    private Long accountId;
    private ImportBatchStatus status;
    private int totalRecords;
    private int successfulRecords;
    private int failedRecords;
    private String errorDetails;
    private String importedBy;
    private Instant createdAt;
    private Instant completedAt;
}
