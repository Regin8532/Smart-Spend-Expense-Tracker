package com.example.ExpenseTracker.service;

import com.example.ExpenseTracker.entity.Budget;
import com.example.ExpenseTracker.repository.BudgetRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

    @Mock BudgetRepository budgetRepo;

    @InjectMocks BudgetService budgetService;

    @Test
    void getBudget_returnsNullWhenNotFound() {
        when(budgetRepo.findByUserIdAndMonthAndCategory(1L, "2026-01", "Food"))
                .thenReturn(Optional.empty());

        assertThat(budgetService.getBudget(1L, "2026-01", "Food")).isNull();
    }

    @Test
    void monthStart_monthEnd_work() {
        LocalDate start = BudgetService.monthStart("2026-02");
        LocalDate end = BudgetService.monthEnd("2026-02");

        assertThat(start).isEqualTo(LocalDate.of(2026, 2, 1));
        assertThat(end).isEqualTo(LocalDate.of(2026, 2, 28));
    }

    @Test
    void isNear_and_isExceeded_work() {
        BigDecimal limit = new BigDecimal("1000");

        assertThat(BudgetService.isNear(new BigDecimal("800"), limit)).isTrue();
        assertThat(BudgetService.isNear(new BigDecimal("799.99"), limit)).isFalse();

        assertThat(BudgetService.isExceeded(new BigDecimal("1000"), limit)).isFalse();
        assertThat(BudgetService.isExceeded(new BigDecimal("1000.01"), limit)).isTrue();
    }

    @Test
    void getBudget_found_returnsBudget() {
        Budget b = new Budget();
        when(budgetRepo.findByUserIdAndMonthAndCategory(1L, "2026-01", "Food"))
                .thenReturn(Optional.of(b));

        assertThat(budgetService.getBudget(1L, "2026-01", "Food")).isSameAs(b);
    }
}
