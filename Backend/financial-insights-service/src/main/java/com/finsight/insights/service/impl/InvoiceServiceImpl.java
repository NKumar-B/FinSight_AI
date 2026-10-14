package com.finsight.insights.service.impl;

import com.finsight.common.context.CorrelationIdContext;
import com.finsight.common.dto.AuditEventDto;
import com.finsight.common.dto.PagedResponse;
import com.finsight.common.exception.ConflictException;
import com.finsight.common.exception.ResourceNotFoundException;
import com.finsight.insights.client.AuditClient;
import com.finsight.insights.dto.request.CreateInvoiceRequest;
import com.finsight.insights.dto.request.RecordPaymentRequest;
import com.finsight.insights.dto.response.AccountsReceivableAgingResponse;
import com.finsight.insights.dto.response.InvoiceResponse;
import com.finsight.insights.entity.Invoice;
import com.finsight.insights.enums.InvoiceStatus;
import com.finsight.insights.mapper.InvoiceMapper;
import com.finsight.insights.repository.InvoiceRepository;
import com.finsight.insights.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private static final Logger log = LoggerFactory.getLogger(InvoiceServiceImpl.class);

    private final InvoiceRepository invoiceRepository;
    private final InvoiceMapper mapper;
    private final AuditClient auditClient;

    @Override
    @Transactional
    public InvoiceResponse createInvoice(CreateInvoiceRequest request, String createdBy) {
        log.info("[Invoices] Creating invoice {} for customer {} by {}",
                request.getInvoiceNumber(), request.getCustomerName(), createdBy);

        if (invoiceRepository.findByInvoiceNumber(request.getInvoiceNumber()).isPresent()) {
            throw new ConflictException("Invoice already exists with number: " + request.getInvoiceNumber());
        }

        InvoiceStatus initialStatus = InvoiceStatus.ISSUED;
        if (request.getDueDate().isBefore(LocalDate.now())) {
            initialStatus = InvoiceStatus.OVERDUE;
        }

        Invoice invoice = Invoice.builder()
                .invoiceNumber(request.getInvoiceNumber())
                .customerId(request.getCustomerId())
                .customerName(request.getCustomerName())
                .issueDate(request.getIssueDate())
                .dueDate(request.getDueDate())
                .amount(request.getAmount())
                .paidAmount(BigDecimal.ZERO)
                .currency(request.getCurrency() != null ? request.getCurrency() : "USD")
                .status(initialStatus)
                .description(request.getDescription())
                .build();

        Invoice saved = invoiceRepository.save(invoice);

        try {
            auditClient.recordAuditEvent(AuditEventDto.builder()
                    .actorId(createdBy)
                    .actorType("USER")
                    .action("INVOICE_CREATED")
                    .entityType("INVOICE")
                    .entityId(String.valueOf(saved.getId()))
                    .correlationId(CorrelationIdContext.get())
                    .outcome("SUCCESS")
                    .afterState(String.format("invoice=%s, amount=%s, status=%s", saved.getInvoiceNumber(), saved.getAmount(), saved.getStatus()))
                    .metadata(Map.of("customerName", saved.getCustomerName()))
                    .build());
        } catch (Exception e) {
            log.warn("[Invoices] Could not record audit event: {}", e.getMessage());
        }

        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public InvoiceResponse recordPayment(Long id, RecordPaymentRequest request, String actor) {
        log.info("[Invoices] Recording payment of {} on invoice id={} by {}", request.getPaymentAmount(), id, actor);

        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + id));

        String beforeState = String.format("paidAmount=%s, status=%s", invoice.getPaidAmount(), invoice.getStatus());

        BigDecimal currentPaid = invoice.getPaidAmount() != null ? invoice.getPaidAmount() : BigDecimal.ZERO;
        BigDecimal newPaid = currentPaid.add(request.getPaymentAmount());

        if (newPaid.compareTo(invoice.getAmount()) >= 0) {
            invoice.setPaidAmount(invoice.getAmount());
            invoice.setStatus(InvoiceStatus.PAID);
        } else {
            invoice.setPaidAmount(newPaid);
            invoice.setStatus(InvoiceStatus.PARTIALLY_PAID);
        }

        Invoice updated = invoiceRepository.save(invoice);
        String afterState = String.format("paidAmount=%s, status=%s", updated.getPaidAmount(), updated.getStatus());

        try {
            auditClient.recordAuditEvent(AuditEventDto.builder()
                    .actorId(actor)
                    .actorType("USER")
                    .action("INVOICE_PAYMENT_RECORDED")
                    .entityType("INVOICE")
                    .entityId(String.valueOf(id))
                    .correlationId(CorrelationIdContext.get())
                    .outcome("SUCCESS")
                    .beforeState(beforeState)
                    .afterState(afterState)
                    .metadata(Map.of("paymentAmount", request.getPaymentAmount()))
                    .build());
        } catch (Exception e) {
            log.warn("[Invoices] Failed to send payment audit event: {}", e.getMessage());
        }

        return mapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<InvoiceResponse> listInvoices(InvoiceStatus status, Pageable pageable) {
        Page<Invoice> page;
        if (status != null) {
            page = invoiceRepository.findByStatus(status, pageable);
        } else {
            page = invoiceRepository.findAll(pageable);
        }

        List<InvoiceResponse> content = page.getContent().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());

        return PagedResponse.<InvoiceResponse>builder()
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
    public InvoiceResponse getInvoiceById(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + id));
        return mapper.toResponse(invoice);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountsReceivableAgingResponse getArAging() {
        List<Invoice> outstanding = invoiceRepository.findOutstandingInvoices();

        LocalDate today = LocalDate.now();
        BigDecimal totalOutstanding = BigDecimal.ZERO;
        BigDecimal currentBucket = BigDecimal.ZERO;    // 0 - 30 days
        BigDecimal bucket31To60 = BigDecimal.ZERO;     // 31 - 60 days
        BigDecimal bucket61To90 = BigDecimal.ZERO;     // 61 - 90 days
        BigDecimal bucket90Plus = BigDecimal.ZERO;     // 90+ days
        long overdueCount = 0;

        for (Invoice inv : outstanding) {
            BigDecimal due = inv.getAmount().subtract(inv.getPaidAmount() != null ? inv.getPaidAmount() : BigDecimal.ZERO).max(BigDecimal.ZERO);
            totalOutstanding = totalOutstanding.add(due);

            if (inv.getDueDate() == null || !today.isAfter(inv.getDueDate())) {
                currentBucket = currentBucket.add(due);
            } else {
                long daysPastDue = ChronoUnit.DAYS.between(inv.getDueDate(), today);
                overdueCount++;

                if (daysPastDue <= 30) {
                    currentBucket = currentBucket.add(due);
                } else if (daysPastDue <= 60) {
                    bucket31To60 = bucket31To60.add(due);
                } else if (daysPastDue <= 90) {
                    bucket61To90 = bucket61To90.add(due);
                } else {
                    bucket90Plus = bucket90Plus.add(due);
                }
            }
        }

        return AccountsReceivableAgingResponse.builder()
                .totalOutstanding(totalOutstanding)
                .currentBucket(currentBucket)
                .bucket31To60(bucket31To60)
                .bucket61To90(bucket61To90)
                .bucket90Plus(bucket90Plus)
                .totalInvoicesCount(outstanding.size())
                .overdueInvoicesCount(overdueCount)
                .build();
    }
}
