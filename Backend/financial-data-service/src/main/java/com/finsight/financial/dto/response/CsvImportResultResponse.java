package com.finsight.financial.dto.response;

import com.finsight.financial.enums.ImportBatchStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CsvImportResultResponse {
    private String batchReference;
    private String fileName;
    private ImportBatchStatus status;
    private int totalRecords;
    private int successfulRecords;
    private int failedRecords;
    private List<String> errorMessages;
    private List<TransactionResponse> importedTransactions;
}
