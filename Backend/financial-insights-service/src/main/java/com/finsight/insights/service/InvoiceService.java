package com.finsight.insights.service;

import com.finsight.common.dto.PagedResponse;
import com.finsight.insights.dto.request.CreateInvoiceRequest;
import com.finsight.insights.dto.request.RecordPaymentRequest;
import com.finsight.insights.dto.response.AccountsReceivableAgingResponse;
import com.finsight.insights.dto.response.InvoiceResponse;
import com.finsight.insights.enums.InvoiceStatus;
import org.springframework.data.domain.Pageable;

public interface InvoiceService {
    InvoiceResponse createInvoice(CreateInvoiceRequest request, String createdBy);
    InvoiceResponse recordPayment(Long id, RecordPaymentRequest request, String actor);
    PagedResponse<InvoiceResponse> listInvoices(InvoiceStatus status, Pageable pageable);
    InvoiceResponse getInvoiceById(Long id);
    AccountsReceivableAgingResponse getArAging();
}
