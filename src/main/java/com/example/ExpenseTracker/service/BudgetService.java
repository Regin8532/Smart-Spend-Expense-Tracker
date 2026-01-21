package com.example.ExpenseTracker.service;

import com.example.ExpenseTracker.entity.Budget;
import com.example.ExpenseTracker.entity.User;
import com.example.ExpenseTracker.repository.BudgetRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

@Service
public class BudgetService {
    private final BudgetRepository budgetRepo;

    public BudgetService(BudgetRepository budgetRepo) {
        this.budgetRepo = budgetRepo;
    }

    public Budget getBudget(Long userId, String month, String category) {
        return budgetRepo.findByUserIdAndMonthAndCategory(userId, month, category).orElse(null);
    }

    public static LocalDate monthStart(String month) {
        YearMonth ym = YearMonth.parse(month);
        return ym.atDay(1);
    }
    public static LocalDate monthEnd(String month) {
        YearMonth ym = YearMonth.parse(month);
        return ym.atEndOfMonth();
    }

    public static boolean isNear(BigDecimal spent, BigDecimal limit) {
        if (limit.compareTo(BigDecimal.ZERO) <= 0) return false;
        return spent.divide(limit, 4, java.math.RoundingMode.HALF_UP)
                .compareTo(new BigDecimal("0.80")) >= 0;
    }
    public static boolean isExceeded(BigDecimal spent, BigDecimal limit) {
        return spent.compareTo(limit) > 0;
    }
}
