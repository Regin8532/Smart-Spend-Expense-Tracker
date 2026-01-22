package com.example.ExpenseTracker.controller;

import com.example.ExpenseTracker.dto.ExpenseDto;
import com.example.ExpenseTracker.entity.Expense;
import com.example.ExpenseTracker.entity.User;
import com.example.ExpenseTracker.repository.ExpenseRepository;
import com.example.ExpenseTracker.repository.UserRepository;
import com.example.ExpenseTracker.service.ExpenseService;
import com.example.ExpenseTracker.service.ReportService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ExpenseControllerUnitTest {

    @Mock ExpenseService expenseService;
    @Mock ExpenseRepository expenseRepo;
    @Mock UserRepository userRepo;
    @Mock ReportService reportService;
    @Mock HttpSession session;
    @Mock Model model;
    @Mock BindingResult br;

    ExpenseController controller;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        controller = new ExpenseController(expenseService, expenseRepo, userRepo, reportService);
        when(session.getAttribute("USER_ID")).thenReturn(1L);
    }

    @Test
    void list_addsExpensesAndFilters_returnsView() {
        when(expenseService.filter(eq(1L), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(new Expense()));

        String view = controller.list(session, null, null, null, null,
                null, null, "dateDesc", model);

        assertThat(view).isEqualTo("expenses/list");
        verify(model).addAttribute(eq("expenses"), anyList());
        verify(model).addAttribute("sort", "dateDesc");
    }

    @Test
    void createPage_setsDefaults_returnsFormView() {
        String view = controller.createPage(model);

        assertThat(view).isEqualTo("expenses/form");
        verify(model).addAttribute(eq("expenseDto"), argThat((ExpenseDto d) ->
                d.getDate() != null && "EXPENSE".equals(d.getType())
        ));
    }

    @Test
    void create_whenErrors_returnsForm() {
        when(br.hasErrors()).thenReturn(true);

        String view = controller.create(new ExpenseDto(), br, session, model);

        assertThat(view).isEqualTo("expenses/form");
        verifyNoInteractions(expenseService);
    }

    @Test
    void create_success_redirectsCreated() {
        when(br.hasErrors()).thenReturn(false);

        User u = new User();
        u.setId(1L);

        when(userRepo.findById(1L)).thenReturn(Optional.of(u));

        ExpenseDto dto = new ExpenseDto();
        dto.setTitle("Burger");
        dto.setAmount(new BigDecimal("100"));
        dto.setCategory("Food");
        dto.setType("EXPENSE");
        dto.setDate(LocalDate.of(2026, 1, 1));

        String view = controller.create(dto, br, session, model);

        assertThat(view).isEqualTo("redirect:/expenses?created");
        verify(expenseService).save(eq(u), eq(dto));
    }

    @Test
    void editPage_notOwner_redirectsForbidden() {
        Expense e = new Expense();
        User other = new User();
        other.setId(99L);
        e.setUser(other);

        when(expenseRepo.findById(5L)).thenReturn(Optional.of(e));

        String view = controller.editPage(5L, session, model);

        assertThat(view).isEqualTo("redirect:/expenses?forbidden");
    }

    @Test
    void update_whenErrors_returnsFormWithExpenseId() {
        when(br.hasErrors()).thenReturn(true);

        String view = controller.update(5L, new ExpenseDto(), br, session, model);

        assertThat(view).isEqualTo("expenses/form");
        verify(model).addAttribute("expenseId", 5L);
    }

    @Test
    void delete_owner_deletesAndRedirects() {
        Expense e = new Expense();
        User owner = new User();
        owner.setId(1L);
        e.setUser(owner);

        when(expenseRepo.findById(5L)).thenReturn(Optional.of(e));

        String view = controller.delete(5L, session);

        assertThat(view).isEqualTo("redirect:/expenses?deleted");
        verify(expenseRepo).deleteById(5L);
    }
}
