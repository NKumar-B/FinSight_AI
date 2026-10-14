package com.finsight.financial.repository;

import com.finsight.financial.entity.Account;
import com.finsight.financial.enums.AccountStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByAccountNumber(String accountNumber);
    boolean existsByAccountNumber(String accountNumber);
    Page<Account> findByStatus(AccountStatus status, Pageable pageable);
    Page<Account> findByAccountNameContainingIgnoreCaseOrAccountNumberContainingIgnoreCase(String name, String number, Pageable pageable);
}
