package com.example.ExpenseTracker.repository;

import com.example.ExpenseTracker.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
    Optional<Budget> findByUserIdAndMonthAndCategory(Long userId, String month, String category);
    List<Budget> findByUserIdAndMonth(Long userId, String month);
}
