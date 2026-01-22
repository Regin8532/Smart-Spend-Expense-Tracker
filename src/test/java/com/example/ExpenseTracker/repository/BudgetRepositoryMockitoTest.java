package com.example.ExpenseTracker.repository;

import com.example.ExpenseTracker.entity.Budget;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class BudgetRepositoryMockitoTest {

    @Test
    void findByUserIdAndMonthAndCategory_returnsOptional() {
        BudgetRepository repo = mock(BudgetRepository.class);

        Budget b = new Budget();
//        b.setId(1L);
        b.setMonth("2026-01");
        b.setCategory("Food");
        b.setLimitAmount(new BigDecimal("1500"));

        when(repo.findByUserIdAndMonthAndCategory(10L, "2026-01", "Food"))
                .thenReturn(Optional.of(b));

        Optional<Budget> out = repo.findByUserIdAndMonthAndCategory(10L, "2026-01", "Food");

        assertThat(out).isPresent();
        assertThat(out.get().getLimitAmount()).isEqualByComparingTo("1500");
        verify(repo).findByUserIdAndMonthAndCategory(10L, "2026-01", "Food");
    }

    @Test
    void findByUserIdAndMonth_returnsList() {
        BudgetRepository repo = mock(BudgetRepository.class);

        when(repo.findByUserIdAndMonth(10L, "2026-01"))
                .thenReturn(List.of(new Budget(), new Budget()));

        List<Budget> list = repo.findByUserIdAndMonth(10L, "2026-01");

        assertThat(list).hasSize(2);
        verify(repo).findByUserIdAndMonth(10L, "2026-01");
    }
}
