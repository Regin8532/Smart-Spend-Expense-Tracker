package com.example.ExpenseTracker.service;

import com.example.ExpenseTracker.dto.ExpenseDto;
import com.example.ExpenseTracker.entity.Budget;
import com.example.ExpenseTracker.entity.Expense;
import com.example.ExpenseTracker.entity.User;
import com.example.ExpenseTracker.repository.ExpenseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    @Mock ExpenseRepository expenseRepo;
    @Mock BudgetService budgetService;
    @Mock NotificationService notificationService;
    @Mock EmailService emailService;

    @InjectMocks ExpenseService expenseService;

    @Test
    void save_income_doesNotCheckBudget() {
        User user = new User();
        user.setId(1L);
        user.setEmail("a@test.com");

        ExpenseDto dto = new ExpenseDto();
        dto.setTitle("Salary");
        dto.setAmount(new BigDecimal("1000"));
        dto.setCategory("Other");
        dto.setType("INCOME");
        dto.setDate(LocalDate.of(2026, 1, 5));

        when(expenseRepo.save(any(Expense.class))).thenAnswer(inv -> inv.getArgument(0));

        expenseService.save(user, dto);

        verify(expenseRepo).save(any(Expense.class));
        verifyNoInteractions(budgetService);
        verifyNoInteractions(notificationService);
        verifyNoInteractions(emailService);
    }

    @Test
    void save_expense_exceeded_sendsNotificationAndEmail() {
        User user = new User();
        user.setId(1L);
        user.setEmail("a@test.com");

        ExpenseDto dto = new ExpenseDto();
        dto.setTitle("Burger");
        dto.setAmount(new BigDecimal("500"));
        dto.setCategory("Food");
        dto.setType("EXPENSE");
        dto.setDate(LocalDate.of(2026, 1, 10));

        when(expenseRepo.save(any(Expense.class))).thenAnswer(inv -> inv.getArgument(0));

        Budget b = new Budget();
        b.setLimitAmount(new BigDecimal("1000"));
        when(budgetService.getBudget(1L, "2026-01", "Food")).thenReturn(b);

        // spent > limit => exceeded
        when(expenseRepo.sumCategoryExpenseInRange(eq(1L), eq("Food"), any(), any()))
                .thenReturn(new BigDecimal("1200"));

        expenseService.save(user, dto);

        verify(notificationService).notifyUser(eq(user), contains("Budget exceeded"));
        verify(emailService).send(eq("a@test.com"), contains("Exceeded"), contains("Spent"));
    }

    @Test
    void save_expense_near_sendsNotificationAndEmail() {
        User user = new User();
        user.setId(1L);
        user.setEmail("a@test.com");

        ExpenseDto dto = new ExpenseDto();
        dto.setTitle("Grocery");
        dto.setAmount(new BigDecimal("100"));
        dto.setCategory("Food");
        dto.setType("EXPENSE");
        dto.setDate(LocalDate.of(2026, 1, 10));

        when(expenseRepo.save(any(Expense.class))).thenAnswer(inv -> inv.getArgument(0));

        Budget b = new Budget();
        b.setLimitAmount(new BigDecimal("1000"));
        when(budgetService.getBudget(1L, "2026-01", "Food")).thenReturn(b);

        // spent = 800 => near
        when(expenseRepo.sumCategoryExpenseInRange(eq(1L), eq("Food"), any(), any()))
                .thenReturn(new BigDecimal("800"));

        expenseService.save(user, dto);

        verify(notificationService).notifyUser(eq(user), contains("Budget nearing limit"));
        verify(emailService).send(eq("a@test.com"), contains("Alert"), contains("Spent"));
    }

    @Test
    void filter_trimsEmptyStrings_toNull() {
        when(expenseRepo.filter(eq(1L), isNull(), isNull(),
                isNull(), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(List.of());

        expenseService.filter(1L, " ", "  ", null, null, null, null, " ");

        verify(expenseRepo).filter(eq(1L), isNull(), isNull(),
                isNull(), isNull(), isNull(), isNull(), isNull());
    }
}
