package com.example.ExpenseTracker.controller;

import com.example.ExpenseTracker.repository.ExpenseRepository;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.ui.Model;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DashboardControllerUnitTest {

    @Mock ExpenseRepository expenseRepo;
    @Mock HttpSession session;
    @Mock Model model;

    DashboardController controller;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        controller = new DashboardController(expenseRepo);
        when(session.getAttribute("USER_ID")).thenReturn(1L);

        when(expenseRepo.sumByTypeInRange(eq(1L), eq("INCOME"), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new BigDecimal("1000"));
        when(expenseRepo.sumByTypeInRange(eq(1L), eq("EXPENSE"), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new BigDecimal("500"));
        when(expenseRepo.sumCategoryExpenseInRange(eq(1L), anyString(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(BigDecimal.ZERO);
    }

    @Test
    void dashboard_returnsView_andAddsModel() {
        String view = controller.dashboard(session, model);

        assertThat(view).isEqualTo("dashboard");
        verify(model).addAttribute(eq("month"), anyString());
        verify(model).addAttribute("income", new BigDecimal("1000"));
        verify(model).addAttribute("expense", new BigDecimal("500"));
        verify(model).addAttribute(eq("catMap"), anyMap());
    }
}
