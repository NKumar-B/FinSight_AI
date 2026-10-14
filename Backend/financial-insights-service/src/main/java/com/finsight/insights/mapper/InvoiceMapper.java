package com.finsight.insights.mapper;

import com.finsight.insights.dto.response.InvoiceResponse;
import com.finsight.insights.entity.Invoice;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Component
public class InvoiceMapper {

    public InvoiceResponse toResponse(Invoice invoice) {
        if (invoice == null) return null;

        BigDecimal paid = invoice.getPaidAmount() != null ? invoice.getPaidAmount() : BigDecimal.ZERO;
        BigDecimal total = invoice.getAmount() != null ? invoice.getAmount() : BigDecimal.ZERO;
        BigDecimal due = total.subtract(paid).max(BigDecimal.ZERO);

        long daysOverdue = 0;
        if (invoice.getDueDate() != null && LocalDate.now().isAfter(invoice.getDueDate())) {
            daysOverdue = ChronoUnit.DAYS.between(invoice.getDueDate(), LocalDate.now());
        }

        return InvoiceResponse.builder()
                .id(invoice.getId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .customerId(invoice.getCustomerId())
                .customerName(invoice.getCustomerName())
                .issueDate(invoice.getIssueDate())
                .dueDate(invoice.getDueDate())
                .amount(total)
                .paidAmount(paid)
                .balanceDue(due)
                .currency(invoice.getCurrency())
                .status(invoice.getStatus())
                .description(invoice.getDescription())
                .daysOverdue(daysOverdue)
                .createdAt(invoice.getCreatedAt())
                .updatedAt(invoice.getUpdatedAt())
                .build();
    }
}
