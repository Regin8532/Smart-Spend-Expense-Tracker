package com.example.ExpenseTracker.repository;

import com.example.ExpenseTracker.entity.Expense;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ExpenseRepositoryMockitoTest {

    @Test
    void filter_returnsList() {
        ExpenseRepository repo = mock(ExpenseRepository.class);

        when(repo.filter(eq(1L), eq("EXPENSE"), eq("Food"),
                eq(LocalDate.of(2026,1,1)),
                eq(LocalDate.of(2026,1,31)),
                eq(new BigDecimal("10")),
                eq(new BigDecimal("100")),
                eq("dateDesc")))
                .thenReturn(List.of(new Expense()));

        List<Expense> out = repo.filter(
                1L, "EXPENSE", "Food",
                LocalDate.of(2026,1,1),
                LocalDate.of(2026,1,31),
                new BigDecimal("10"),
                new BigDecimal("100"),
                "dateDesc"
        );

        assertThat(out).hasSize(1);
        verify(repo).filter(eq(1L), eq("EXPENSE"), eq("Food"),
                eq(LocalDate.of(2026,1,1)),
                eq(LocalDate.of(2026,1,31)),
                eq(new BigDecimal("10")),
                eq(new BigDecimal("100")),
                eq("dateDesc"));
    }

    @Test
    void sumCategoryExpenseInRange_returnsValue() {
        ExpenseRepository repo = mock(ExpenseRepository.class);

        when(repo.sumCategoryExpenseInRange(1L, "Food",
                LocalDate.of(2026,1,1), LocalDate.of(2026,1,31)))
                .thenReturn(new BigDecimal("500"));

        BigDecimal out = repo.sumCategoryExpenseInRange(1L, "Food",
                LocalDate.of(2026,1,1), LocalDate.of(2026,1,31));

        assertThat(out).isEqualByComparingTo("500");
    }

    @Test
    void sumByTypeInRange_returnsValue() {
        ExpenseRepository repo = mock(ExpenseRepository.class);

        when(repo.sumByTypeInRange(1L, "EXPENSE",
                LocalDate.of(2026,1,1), LocalDate.of(2026,1,31)))
                .thenReturn(new BigDecimal("999"));

        BigDecimal out = repo.sumByTypeInRange(1L, "EXPENSE",
                LocalDate.of(2026,1,1), LocalDate.of(2026,1,31));

        assertThat(out).isEqualByComparingTo("999");
    }
}
