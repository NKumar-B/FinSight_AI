package com.finsight.insights.service;

import com.finsight.insights.client.AuditClient;
import com.finsight.insights.dto.request.CreateInvoiceRequest;
import com.finsight.insights.dto.request.RecordPaymentRequest;
import com.finsight.insights.dto.response.AccountsReceivableAgingResponse;
import com.finsight.insights.dto.response.InvoiceResponse;
import com.finsight.insights.entity.Invoice;
import com.finsight.insights.enums.InvoiceStatus;
import com.finsight.insights.mapper.InvoiceMapper;
import com.finsight.insights.repository.InvoiceRepository;
import com.finsight.insights.service.impl.InvoiceServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private AuditClient auditClient;

    @Spy
    private InvoiceMapper mapper = new InvoiceMapper();

    @InjectMocks
    private InvoiceServiceImpl invoiceService;

    @Test
    void createInvoice_success() {
        CreateInvoiceRequest request = CreateInvoiceRequest.builder()
                .invoiceNumber("INV-2026-001")
                .customerId(10L)
                .customerName("Acme Corp")
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(30))
                .amount(new BigDecimal("5000.00"))
                .currency("USD")
                .build();

        when(invoiceRepository.findByInvoiceNumber("INV-2026-001")).thenReturn(Optional.empty());
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(i -> {
            Invoice inv = i.getArgument(0);
            inv.setId(1L);
            return inv;
        });

        InvoiceResponse response = invoiceService.createInvoice(request, "accountant_jane");

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getInvoiceNumber()).isEqualTo("INV-2026-001");
        assertThat(response.getBalanceDue()).isEqualByComparingTo("5000.00");
        assertThat(response.getStatus()).isEqualTo(InvoiceStatus.ISSUED);

        verify(invoiceRepository).save(any(Invoice.class));
    }

    @Test
    void recordPayment_fullPayment_marksPaid() {
        Invoice invoice = Invoice.builder()
                .id(2L)
                .invoiceNumber("INV-2026-002")
                .customerId(10L)
                .customerName("Acme Corp")
                .amount(new BigDecimal("1000.00"))
                .paidAmount(BigDecimal.ZERO)
                .status(InvoiceStatus.ISSUED)
                .build();

        when(invoiceRepository.findById(2L)).thenReturn(Optional.of(invoice));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(i -> i.getArgument(0));

        RecordPaymentRequest paymentReq = RecordPaymentRequest.builder()
                .paymentAmount(new BigDecimal("1000.00"))
                .build();

        InvoiceResponse response = invoiceService.recordPayment(2L, paymentReq, "accountant_jane");

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(InvoiceStatus.PAID);
        assertThat(response.getBalanceDue()).isEqualByComparingTo("0.00");
    }

    @Test
    void getArAging_computesBucketsCorrectly() {
        LocalDate today = LocalDate.now();

        // Inv 1: current (due in 10 days) -> balance 1000
        Invoice invCurrent = Invoice.builder()
                .id(1L)
                .amount(new BigDecimal("1000.00"))
                .paidAmount(BigDecimal.ZERO)
                .dueDate(today.plusDays(10))
                .status(InvoiceStatus.ISSUED)
                .build();

        // Inv 2: overdue 20 days -> bucket 0-30, balance 2000
        Invoice invPast20 = Invoice.builder()
                .id(2L)
                .amount(new BigDecimal("2000.00"))
                .paidAmount(BigDecimal.ZERO)
                .dueDate(today.minusDays(20))
                .status(InvoiceStatus.OVERDUE)
                .build();

        // Inv 3: overdue 45 days -> bucket 31-60, balance 3000
        Invoice invPast45 = Invoice.builder()
                .id(3L)
                .amount(new BigDecimal("3000.00"))
                .paidAmount(BigDecimal.ZERO)
                .dueDate(today.minusDays(45))
                .status(InvoiceStatus.OVERDUE)
                .build();

        // Inv 4: overdue 100 days -> bucket 90+, balance 4000
        Invoice invPast100 = Invoice.builder()
                .id(4L)
                .amount(new BigDecimal("4000.00"))
                .paidAmount(BigDecimal.ZERO)
                .dueDate(today.minusDays(100))
                .status(InvoiceStatus.OVERDUE)
                .build();

        when(invoiceRepository.findOutstandingInvoices())
                .thenReturn(List.of(invCurrent, invPast20, invPast45, invPast100));

        AccountsReceivableAgingResponse aging = invoiceService.getArAging();

        assertThat(aging).isNotNull();
        assertThat(aging.getTotalOutstanding()).isEqualByComparingTo("10000.00");
        assertThat(aging.getCurrentBucket()).isEqualByComparingTo("3000.00"); // 1000 + 2000
        assertThat(aging.getBucket31To60()).isEqualByComparingTo("3000.00");
        assertThat(aging.getBucket90Plus()).isEqualByComparingTo("4000.00");
        assertThat(aging.getOverdueInvoicesCount()).isEqualTo(3);
        assertThat(aging.getTotalInvoicesCount()).isEqualTo(4);
    }
}
