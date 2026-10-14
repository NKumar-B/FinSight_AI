package com.finsight.reconciliation.service;

import com.finsight.common.dto.PagedResponse;
import com.finsight.reconciliation.dto.request.ExecuteReconciliationRequest;
import com.finsight.reconciliation.dto.response.ReconciliationDiscrepancyResponse;
import com.finsight.reconciliation.dto.response.ReconciliationMatchResponse;
import com.finsight.reconciliation.dto.response.ReconciliationRunResponse;
import org.springframework.data.domain.Pageable;

public interface ReconciliationService {
    ReconciliationRunResponse executeReconciliation(ExecuteReconciliationRequest request, String executedBy);
    PagedResponse<ReconciliationRunResponse> listRuns(Pageable pageable);
    ReconciliationRunResponse getRunById(Long id);
    PagedResponse<ReconciliationMatchResponse> getMatchesByRun(Long runId, Pageable pageable);
    PagedResponse<ReconciliationDiscrepancyResponse> getDiscrepanciesByRun(Long runId, Pageable pageable);
}
