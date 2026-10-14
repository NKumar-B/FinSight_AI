package com.finsight.financial.service;

import com.finsight.common.dto.PagedResponse;
import com.finsight.financial.dto.response.CsvImportResultResponse;
import com.finsight.financial.dto.response.ImportBatchResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface FinancialImportService {
    CsvImportResultResponse importCsv(MultipartFile file, Long accountId, String importedBy);
    PagedResponse<ImportBatchResponse> listBatches(Pageable pageable);
    ImportBatchResponse getBatchById(Long id);
}
