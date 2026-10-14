package com.finsight.insights.repository;

import com.finsight.insights.entity.Invoice;
import com.finsight.insights.enums.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    Page<Invoice> findByStatus(InvoiceStatus status, Pageable pageable);

    Page<Invoice> findByCustomerId(Long customerId, Pageable pageable);

    @Query("SELECT i FROM Invoice i WHERE i.status IN ('ISSUED', 'PARTIALLY_PAID', 'OVERDUE')")
    List<Invoice> findOutstandingInvoices();

    @Query("SELECT i FROM Invoice i WHERE i.status IN ('ISSUED', 'PARTIALLY_PAID') AND i.dueDate < :currentDate")
    List<Invoice> findOverdueCandidates(@Param("currentDate") LocalDate currentDate);
}
