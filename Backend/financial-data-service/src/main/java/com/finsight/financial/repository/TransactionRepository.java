package com.finsight.financial.repository;

import com.finsight.financial.entity.Transaction;
import com.finsight.financial.enums.TransactionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {
    Optional<Transaction> findByTransactionReference(String transactionReference);
    Optional<Transaction> findByFingerprint(String fingerprint);
    boolean existsByFingerprint(String fingerprint);
    boolean existsByAccountIdAndTransactionReference(Long accountId, String transactionReference);

    Page<Transaction> findByStatus(TransactionStatus status, Pageable pageable);
    List<Transaction> findByStatus(TransactionStatus status);

    Page<Transaction> findByAccountIdAndTransactionDateBetween(
            Long accountId, LocalDate startDate, LocalDate endDate, Pageable pageable);

    List<Transaction> findByAccountIdOrderByTransactionDateDesc(Long accountId);
}
