package com.finsight.audit.service;

import com.finsight.common.dto.PagedResponse;
import com.finsight.audit.dto.request.GenerateReportRequest;
import com.finsight.audit.dto.response.ComplianceReportResponse;
import org.springframework.data.domain.Pageable;

public interface ComplianceReportService {
    ComplianceReportResponse generateReport(GenerateReportRequest request, String generatedBy);
    PagedResponse<ComplianceReportResponse> listReports(Pageable pageable);
    ComplianceReportResponse getReportById(Long id);
}
