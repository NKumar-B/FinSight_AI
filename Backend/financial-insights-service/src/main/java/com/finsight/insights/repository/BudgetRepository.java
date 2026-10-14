package com.finsight.insights.repository;

import com.finsight.insights.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {
    List<Budget> findByPeriod(String period);
    Optional<Budget> findByCategoryAndPeriod(String category, String period);
}
